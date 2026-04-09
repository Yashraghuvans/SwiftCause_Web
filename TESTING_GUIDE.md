# Magic Link Testing Guide

## Prerequisites

- Node.js installed
- Firebase CLI installed (`firebase --version` should work)
- All dependencies installed (`npm install`)

## Quick Start

### 1. Start Firebase Emulators

Open a terminal and run:

```bash
cd backend
firebase emulators:start
```

This will start:

- Firestore Emulator: http://127.0.0.1:8080
- Functions Emulator: http://127.0.0.1:5001
- Auth Emulator: http://127.0.0.1:9099
- Emulator UI: http://127.0.0.1:4000

**Keep this terminal running!**

### 2. Start Next.js Development Server

Open a **new terminal** and run:

```bash
npm run dev
```

This starts your app at: http://localhost:3000

**Keep this terminal running too!**

### 3. Test the Endpoints

Open a **third terminal** and run:

```bash
node test-magic-link.js
```

This tests the validation and consumption endpoints.

---

## Manual Testing Flow

### Option A: Test with Real Donation Flow

1. **Make sure emulators are running** (Step 1 above)

2. **Create a test campaign** in the emulator:
   - Go to http://localhost:3000/admin
   - Create a campaign with Gift Aid enabled

3. **Make a test donation**:
   - Go to the campaign page
   - Enter donation amount
   - Complete the donation flow with Gift Aid

4. **Check the Firestore Emulator**:
   - Open http://127.0.0.1:4000
   - Navigate to Firestore
   - Look for `magicLinkTokens` collection
   - You should see a generated token

5. **Get the plain token**:
   - In Firestore emulator, go to `magicLinkEphemeral` collection
   - Find document with ID matching your payment intent
   - Look for `plainToken` field (available for 2 minutes)
   - Copy this value

6. **Test the magic link**:
   - Visit: `http://localhost:3000/link/{paste-token-here}`
   - You should see the validation page
   - It should redirect to the Gift Aid form

### Option B: Test with Manual Token Creation

1. **Open Firestore Emulator UI**: http://127.0.0.1:4000

2. **Create test tokens manually**:
   - Go to Firestore tab
   - Create collection: `magicLinkTokens`
   - Add document with ID: `test_payment_intent_123`
   - Add fields:
     ```json
     {
       "tokenHash": "abc123...",
       "donationId": "test_payment_intent_123",
       "campaignId": "test_campaign_id",
       "amount": 5000,
       "currency": "gbp",
       "purpose": "gift_aid",
       "status": "active",
       "expiresAt": "2026-05-06T00:00:00.000Z",
       "createdAt": "2026-04-06T00:00:00.000Z",
       "validationAttempts": 0,
       "blocked": false
     }
     ```
   - Create collection: `magicLinkEphemeral`
   - Add document with same ID: `test_payment_intent_123`
   - Add fields:
     ```json
     {
       "plainToken": "your-test-token-here",
       "expiresAt": "2026-04-06T00:05:00.000Z",
       "createdAt": "2026-04-06T00:00:00.000Z"
     }
     ```

3. **Test validation**:

   ```bash
   curl -X POST http://127.0.0.1:5001/swiftcause-app/us-central1/validateMagicLinkToken \
     -H "Content-Type: application/json" \
     -d '{"token":"your-test-token-here"}'
   ```

4. **Test consumption**:
   ```bash
   curl -X POST http://127.0.0.1:5001/swiftcause-app/us-central1/consumeMagicLinkToken \
     -H "Content-Type: application/json" \
     -d '{"tokenId":"test_payment_intent_123"}'
   ```

---

## Testing Checklist

### Token Generation (Webhook)

- [ ] Token generated after successful donation
- [ ] Token stored in Firestore with correct fields
- [ ] Token hash is SHA-256 of plain token
- [ ] Expiry set to 30 days from now
- [ ] Status is "active"
- [ ] Purpose determined correctly (gift_aid, gift_aid_and_recurring, or recurring)

### Token Validation

- [ ] Valid token returns 200 with donation details
- [ ] Invalid token returns 404 TOKEN_NOT_FOUND
- [ ] Expired token returns 410 TOKEN_EXPIRED
- [ ] Consumed token returns 410 TOKEN_CONSUMED
- [ ] Blocked token returns 403 TOKEN_BLOCKED
- [ ] Failed attempts increment validationAttempts
- [ ] Token blocked after 5 failed attempts
- [ ] Successful validation does NOT increment attempts

### Token Consumption

- [ ] Active token can be consumed
- [ ] Consumed token status changes to "consumed"
- [ ] completedAt timestamp is set
- [ ] Already consumed token returns error
- [ ] Expired token cannot be consumed
- [ ] Transaction prevents race conditions

### Frontend (Landing Page)

- [ ] Loading spinner shows during validation
- [ ] Error messages display correctly
- [ ] Retry button works
- [ ] Redirects to campaign page with correct params
- [ ] Amount converted from pence to pounds
- [ ] Token ID passed in URL for consumption

### Gift Aid Flow Integration

- [ ] Token consumed after form submission
- [ ] Non-blocking (doesn't fail user flow)
- [ ] Works with or without token parameter

---

## Troubleshooting

### Emulators won't start

```bash
# Kill any existing processes
npx kill-port 4000 5001 8080 9099 9199

# Try again
cd backend
firebase emulators:start
```

### Functions not loading

```bash
# Install dependencies
cd backend/functions
npm install

# Go back and restart emulators
cd ..
firebase emulators:start
```

### Environment variable not working

- Make sure `.env` has `NEXT_PUBLIC_USE_FIREBASE_EMULATORS=true`
- Restart Next.js dev server after changing `.env`
- Check browser console for the functions URL being used

### CORS errors

- Emulators should handle CORS automatically
- If issues persist, check the ALLOWED_ORIGINS in `backend/functions/index.js`
- Add `http://localhost:3000` if not present

---

## Production Testing

To test against production (after deployment):

1. **Remove emulator flag** from `.env`:

   ```bash
   # Comment out or remove this line
   # NEXT_PUBLIC_USE_FIREBASE_EMULATORS=true
   ```

2. **Restart Next.js**:

   ```bash
   npm run dev
   ```

3. **Functions will now call production**:
   - https://us-central1-swiftcause-app.cloudfunctions.net/validateMagicLinkToken
   - https://us-central1-swiftcause-app.cloudfunctions.net/consumeMagicLinkToken

---

## Useful Commands

```bash
# View emulator logs
cd backend
firebase emulators:start --debug

# Export emulator data
firebase emulators:export ./emulator-data

# Import emulator data
firebase emulators:start --import=./emulator-data

# Run only specific emulators
firebase emulators:start --only functions,firestore

# Clear emulator data
rm -rf backend/.firebase
```

---

## Next Steps After Testing

1. ✅ Verify all tests pass locally
2. 🚀 Deploy functions: `firebase deploy --only functions`
3. 📊 Deploy Firestore indexes: `firebase deploy --only firestore:indexes`
4. 🧪 Test in production with real Stripe test mode
5. 📧 Set up email notifications (future enhancement)
6. 📱 Test Android integration (future)
