# Firestore Rules Guide

## Overview

This project uses **separate Firestore rules** for production and emulator environments to ensure security while maintaining developer productivity.

## File Structure

```
backend/
├── firestore.rules           ← 🔒 Production (STRICT)
├── firestore.emulator.rules  ← 🔓 Emulator (RELAXED)
└── firebase.json             ← Configuration
```

## Rules Files

### 1. `firestore.rules` (Production)

**Purpose**: Deployed to production Firebase project

**Security Level**: STRICT

**Features**:

- ✅ Role-based access control (admin, user)
- ✅ Owner-based permissions
- ✅ Backend-only collections (donations, webhooks)
- ✅ Time-restricted access (magic link ephemeral)
- ✅ Default deny (secure by default)
- ❌ NO open access patterns

**Used By**:

- `firebase deploy --only firestore:rules`
- Production Firebase project

### 2. `firestore.emulator.rules` (Emulator)

**Purpose**: Used by Firebase Emulators for local development

**Security Level**: RELAXED

**Features**:

- ✅ Same magic link security (for testing)
- ✅ Open access to other collections (easy testing)
- ⚠️ Contains `allow read, write: if true` for development
- ⚠️ NEVER deployed to production

**Used By**:

- `firebase emulators:start`
- Local development only

## Configuration

### `firebase.json`

```json
{
  "firestore": {
    "rules": "firestore.rules" // ← Production
  },
  "emulators": {
    "firestore": {
      "rules": "firestore.emulator.rules" // ← Emulator
    }
  }
}
```

**Key Points**:

- Production uses `firestore.rules`
- Emulator uses `firestore.emulator.rules`
- Emulator rules are NEVER deployed

## NPM Scripts

### Development

```bash
# Start emulators (uses firestore.emulator.rules)
npm run emulator
```

### Deployment

```bash
# Deploy functions only
npm run deploy:functions

# Deploy rules only (uses firestore.rules)
npm run deploy:rules

# Deploy everything
npm run deploy:all
```

### Verification

```bash
# Verify production rules are secure
npm run verify:rules
```

## Safety Mechanisms

### 1. Separate Files

- Production rules in `firestore.rules`
- Emulator rules in `firestore.emulator.rules`
- No risk of accidental deployment

### 2. Clear Warnings

Both files have prominent warnings:

**Production (`firestore.rules`)**:

```javascript
// 🔒 PRODUCTION FIRESTORE RULES
// These rules are deployed to production and enforce strict security.
```

**Emulator (`firestore.emulator.rules`)**:

```javascript
// ⚠️⚠️⚠️ EMULATOR ONLY - DO NOT DEPLOY TO PRODUCTION ⚠️⚠️⚠️
```

### 3. Verification Script

Run before deployment:

```bash
npm run verify:rules
```

**Checks**:

- ✅ Production rules don't have `allow read, write: if true`
- ✅ Production rules don't have wildcard open access
- ✅ `firebase.json` points to correct files
- ✅ Both files exist

**Output**:

```
🔍 Firestore Rules Verification

Checking: Production Rules
File: backend/firestore.rules

✅ No dangerous patterns found

Checking: Emulator Rules
File: backend/firestore.emulator.rules

⚠️  INFO: Open read/write access (allow read, write: if true)
   This is expected for emulator rules

✅ VERIFICATION PASSED
Production rules are secure!
```

### 4. CI/CD Integration

Add to your CI/CD pipeline (e.g., GitHub Actions):

```yaml
# .github/workflows/deploy.yml
- name: Verify Firestore Rules
  run: npm run verify:rules

- name: Deploy to Firebase
  run: npm run deploy:all
```

## Production Rules Structure

### Magic Link Collections

```javascript
// Secure collection (backend only)
match /magicLinkTokens/{tokenId} {
  allow read, write: if false;
}

// Ephemeral collection (time-restricted)
match /magicLinkEphemeral/{tokenId} {
  allow read: if
    resource.data.expiresAt != null &&
    request.time < resource.data.expiresAt;
  allow write: if false;
}
```

### User Collections

```javascript
match /users/{userId} {
  // Users can read their own profile
  allow read: if isOwner(userId) || isAdmin();

  // Users can update their own profile (except role)
  allow update: if isOwner(userId) &&
                   !request.resource.data.diff(resource.data).affectedKeys().hasAny(['role']);

  // Only admins can create/delete users
  allow create, delete: if isAdmin();
}
```

### Backend-Only Collections

```javascript
match /donations/{donationId} {
  allow read: if isAdmin();
  allow write: if false;  // Backend only
}

match /webhookEvents/{eventId} {
  allow read, write: if false;  // Backend only
}
```

## Emulator Rules Structure

Same as production, but with catch-all:

```javascript
// Same magic link rules as production
match /magicLinkTokens/{tokenId} { ... }
match /magicLinkEphemeral/{tokenId} { ... }

// Open access for other collections (testing)
match /{document=**} {
  allow read, write: if true;
}
```

## Common Workflows

### Local Development

1. Start emulators:

   ```bash
   npm run emulator
   ```

2. Emulator uses `firestore.emulator.rules`
3. No authentication friction
4. Easy testing

### Production Deployment

1. Verify rules:

   ```bash
   npm run verify:rules
   ```

2. Deploy rules:

   ```bash
   npm run deploy:rules
   ```

3. Deploy functions:
   ```bash
   npm run deploy:functions
   ```

### Adding New Collection

1. Add rules to `firestore.rules` (production)
2. Test with emulator (uses `firestore.emulator.rules`)
3. Verify rules: `npm run verify:rules`
4. Deploy: `npm run deploy:rules`

## Troubleshooting

### Issue: "Missing or insufficient permissions" in emulator

**Cause**: Emulator is using production rules

**Fix**: Check `firebase.json`:

```json
"emulators": {
  "firestore": {
    "rules": "firestore.emulator.rules"  // ← Must be set
  }
}
```

### Issue: Verification script fails

**Cause**: Production rules contain dangerous patterns

**Fix**: Remove `allow read, write: if true` from `firestore.rules`

### Issue: Emulator rules deployed to production

**Cause**: Incorrect `firebase.json` configuration

**Fix**: Ensure production uses `firestore.rules`:

```json
"firestore": {
  "rules": "firestore.rules"  // ← Production
}
```

## Best Practices

### 1. Always Verify Before Deploy

```bash
npm run verify:rules && npm run deploy:rules
```

### 2. Never Edit Emulator Rules for Production

- Edit `firestore.rules` for production changes
- Keep `firestore.emulator.rules` simple and open

### 3. Test Locally First

```bash
# Start emulator
npm run emulator

# Test your changes
# ...

# Verify rules
npm run verify:rules

# Deploy
npm run deploy:rules
```

### 4. Use Helper Functions

```javascript
function isAuthenticated() {
  return request.auth != null;
}

function isAdmin() {
  return isAuthenticated() &&
         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['admin', 'super_admin'];
}

function isOwner(userId) {
  return isAuthenticated() && request.auth.uid == userId;
}
```

### 5. Default Deny

Don't add catch-all rules to production:

```javascript
// ❌ BAD (in production)
match /{document=**} {
  allow read, write: if true;
}

// ✅ GOOD (default deny)
// No catch-all rule = secure by default
```

## Security Checklist

Before deploying to production:

- [ ] Run `npm run verify:rules`
- [ ] No `allow read, write: if true` in `firestore.rules`
- [ ] No wildcard open access in `firestore.rules`
- [ ] `firebase.json` points to `firestore.rules` for production
- [ ] `firebase.json` points to `firestore.emulator.rules` for emulator
- [ ] All sensitive collections have `allow write: if false`
- [ ] Magic link collections use time-restricted access
- [ ] Helper functions are defined and used
- [ ] Default deny is in effect (no catch-all)

## References

- [Firestore Security Rules Documentation](https://firebase.google.com/docs/firestore/security/get-started)
- [Firebase Emulator Suite](https://firebase.google.com/docs/emulator-suite)
- Magic Link Security: `SECURITY_ANALYSIS.md`
- Deployment Guide: `DEPLOYMENT_CHECKLIST.md`
