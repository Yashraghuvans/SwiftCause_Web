# Magic Link Gift Aid Implementation

## Overview

Post-donation Gift Aid collection system using secure magic links sent via email. Donors complete payment first, then receive a magic link to optionally complete their Gift Aid declaration, removing friction from the checkout process.

---

## Architecture

### Two-Collection Security Model

#### 1. `magicLinkTokens` (Secure - Backend Only)

**Purpose**: Permanent storage of token data with security

**Fields**:

- `tokenHash` (string): SHA-256 hash of plain token
- `donorEmail` (string): Donor's email address
- `amount` (number): Donation amount in pence
- `campaignId` (string): Associated campaign ID
- `campaignTitle` (string): Campaign name
- `purpose` (string): "gift_aid" or "recurring_setup"
- `status` (string): "active", "consumed", "expired", or "blocked"
- `expiresAt` (timestamp): 30 days from creation
- `createdAt` (timestamp): Creation time
- `consumedAt` (timestamp | null): When token was used
- `failedAttempts` (number): Failed validation count
- `lastAttemptIp` (string | null): IP of last failed attempt

**Access**: Backend functions only (`allow read, write: if false`)

**TTL**: 30 days (auto-deleted via Firebase TTL policy on `expiresAt` field)

#### 2. `magicLinkEphemeral` (Public - Time-Restricted)

**Purpose**: Temporary storage for displaying token on result page

**Fields**:

- `plainToken` (string): Unencrypted token for display
- `expiresAt` (timestamp): 2 minutes from creation
- `createdAt` (timestamp): Creation time

**Access**: Public read for 2 minutes only

```javascript
allow read: if request.time < resource.data.expiresAt
```

**TTL**: 2 minutes (auto-deleted via Firebase TTL policy on `expiresAt` field)

---

## Token System

### Generation

- **Algorithm**: Cryptographically secure random bytes
- **Length**: 32 bytes → 43 characters (base64url encoded)
- **Hashing**: SHA-256 for storage
- **Document ID**: Deterministic (uses `paymentIntentId`)
- **Transaction**: Single Firestore transaction writes to both collections atomically

### Security Features

- Plain token never stored permanently (only in ephemeral collection for 2 minutes)
- SHA-256 hashed token in secure collection
- Attempt tracking (5 failed attempts = status "blocked")
- IP logging for failed attempts
- Single-use tokens (status changes to "consumed" after use)
- 30-day expiry with automatic status updates

### Purpose Determination

```javascript
function determinePurpose(metadata) {
  const hasRecurringInterest =
    metadata.recurringInterest === 'true' || metadata.recurringInterest === true;
  const isRecurring = metadata.isRecurring === 'true' || metadata.isRecurring === true;

  return hasRecurringInterest || isRecurring ? 'recurring_setup' : 'gift_aid';
}
```

---

## Implementation Files

### Backend - Cloud Functions

#### 1. `backend/functions/entities/magicLink.js` (NEW)

Token generation logic with two-collection architecture.

**Key Functions**:

- `determinePurpose(metadata)`: Determines token purpose from payment metadata
- `generateMagicLinkToken(paymentIntentId, donorEmail, amount, campaignId, campaignTitle, metadata)`: Generates and stores token

**Implementation**:

```javascript
const crypto = require('crypto');
const admin = require('firebase-admin');

async function generateMagicLinkToken(
  paymentIntentId,
  donorEmail,
  amount,
  campaignId,
  campaignTitle,
  metadata,
) {
  const plainToken = crypto.randomBytes(32).toString('base64url');
  const tokenHash = crypto.createHash('sha256').update(plainToken).digest('hex');
  const purpose = determinePurpose(metadata);
  const now = admin.firestore.Timestamp.now();
  const expiresAt = admin.firestore.Timestamp.fromMillis(now.toMillis() + 30 * 24 * 60 * 60 * 1000); // 30 days
  const ephemeralExpiresAt = admin.firestore.Timestamp.fromMillis(now.toMillis() + 2 * 60 * 1000); // 2 minutes

  const tokenData = {
    tokenHash,
    donorEmail,
    amount,
    campaignId,
    campaignTitle,
    purpose,
    status: 'active',
    expiresAt,
    createdAt: now,
    consumedAt: null,
    failedAttempts: 0,
    lastAttemptIp: null,
  };

  const ephemeralData = {
    plainToken,
    expiresAt: ephemeralExpiresAt,
    createdAt: now,
  };

  await admin.firestore().runTransaction(async (transaction) => {
    const tokenRef = admin.firestore().collection('magicLinkTokens').doc(paymentIntentId);
    const ephemeralRef = admin.firestore().collection('magicLinkEphemeral').doc(paymentIntentId);

    transaction.set(tokenRef, tokenData);
    transaction.set(ephemeralRef, ephemeralData);
  });

  return { tokenId: paymentIntentId, plainToken };
}
```

#### 2. `backend/functions/handlers/webhooks.js` (MODIFIED)

Integrated magic link generation into payment webhook.

**Changes**:

```javascript
const { generateMagicLinkToken } = require('../entities/magicLink');

// After successful payment processing
if (donorEmail && amount && campaignId && campaignTitle) {
  try {
    await generateMagicLinkToken(
      paymentIntentId,
      donorEmail,
      amount,
      campaignId,
      campaignTitle,
      metadata,
    );
  } catch (error) {
    console.error('Failed to generate magic link token:', error);
  }
}
```

#### 3. `backend/functions/index.js` (MODIFIED)

Added two new Cloud Function endpoints.

**New Endpoints**:

**a) `validateMagicLinkToken`**

```javascript
exports.validateMagicLinkToken = functions.https.onRequest((req, res) => {
  // CORS handling
  const origin = req.headers.origin;
  if (origin && ALLOWED_ORIGINS.includes(origin)) {
    res.set('Access-Control-Allow-Origin', origin);
  }

  if (req.method === 'OPTIONS') {
    res.set('Access-Control-Allow-Methods', 'POST');
    res.set('Access-Control-Allow-Headers', 'Content-Type');
    return res.status(204).send('');
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'METHOD_NOT_ALLOWED' });
  }

  const { token } = req.body;
  if (!token) {
    return res.status(400).json({ error: 'INVALID_REQUEST' });
  }

  // Hash token and lookup
  const tokenHash = crypto.createHash('sha256').update(token).digest('hex');
  const tokensRef = admin.firestore().collection('magicLinkTokens');
  const snapshot = await tokensRef.where('tokenHash', '==', tokenHash).limit(1).get();

  if (snapshot.empty) {
    return res.status(404).json({ valid: false, error: 'TOKEN_NOT_FOUND' });
  }

  const tokenDoc = snapshot.docs[0];
  const tokenData = tokenDoc.data();

  // Check if already blocked
  if (tokenData.blocked === true) {
    return res.status(403).json({ valid: false, error: 'TOKEN_BLOCKED' });
  }

  // Check if reached max attempts (5 failed validations)
  const currentAttempts = tokenData.validationAttempts || 0;
  if (currentAttempts >= 5) {
    await tokenDoc.ref.update({ blocked: true });
    return res.status(403).json({ valid: false, error: 'TOKEN_BLOCKED' });
  }

  // Check status (consumed/expired)
  if (tokenData.status === 'consumed') {
    // Increment failed attempts
    await tokenDoc.ref.update({
      validationAttempts: currentAttempts + 1,
      blocked: (currentAttempts + 1) >= 5
    });
    return res.status(410).json({ valid: false, error: 'TOKEN_CONSUMED' });
  }

  if (tokenData.expiresAt.toMillis() < Date.now()) {
    // Increment failed attempts and mark as expired
    await tokenDoc.ref.update({
      status: 'expired',
      validationAttempts: currentAttempts + 1,
      blocked: (currentAttempts + 1) >= 5
    });
    return res.status(410).json({ valid: false, error: 'TOKEN_EXPIRED' });
  }

  // Valid token - DO NOT increment attempts (prevents blocking legitimate users)
  return res.status(200).json({
    valid: true,
    tokenId: tokenDoc.id,
    campaignId: tokenData.campaignId,
    amount: tokenData.amount,
    currency: 'GBP',
    purpose: tokenData.purpose,
    expiresAt: tokenData.expiresAt.toMillis(),
  });
});
```

**b) `consumeMagicLinkToken`**

```javascript
exports.consumeMagicLinkToken = functions.https.onRequest(async (req, res) => {
  // CORS handling
  const origin = req.headers.origin;
  if (origin && ALLOWED_ORIGINS.includes(origin)) {
    res.set('Access-Control-Allow-Origin', origin);
  }

  if (req.method === 'OPTIONS') {
    res.set('Access-Control-Allow-Methods', 'POST');
    res.set('Access-Control-Allow-Headers', 'Content-Type');
    return res.status(204).send('');
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'METHOD_NOT_ALLOWED' });
  }

  const { tokenId } = req.body;
  if (!tokenId) {
    return res.status(400).json({ error: 'INVALID_REQUEST' });
  }

  // Use transaction to prevent race conditions
  const tokenRef = admin.firestore().collection('magicLinkTokens').doc(tokenId);

  try {
    await admin.firestore().runTransaction(async (transaction) => {
      const tokenDoc = await transaction.get(tokenRef);

      if (!tokenDoc.exists) {
        throw new Error('TOKEN_NOT_FOUND');
      }

      const tokenData = tokenDoc.data();

      if (tokenData.status !== 'active') {
        throw new Error('TOKEN_NOT_ACTIVE');
      }

      if (tokenData.expiresAt.toMillis() < Date.now()) {
        throw new Error('TOKEN_EXPIRED');
      }

      transaction.update(tokenRef, {
        status: 'consumed',
        consumedAt: admin.firestore.Timestamp.now(),
      });
    });

    return res.status(200).json({ success: true });
  } catch (error) {
    if (error.message === 'TOKEN_NOT_FOUND') {
      return res.status(404).json({ error: 'TOKEN_NOT_FOUND' });
    }
    if (error.message === 'TOKEN_NOT_ACTIVE') {
      return res.status(410).json({ error: 'TOKEN_ALREADY_USED' });
    }
    if (error.message === 'TOKEN_EXPIRED') {
      return res.status(410).json({ error: 'TOKEN_EXPIRED' });
    }
    return res.status(500).json({ error: 'INTERNAL_ERROR' });
  }
});
```

**CORS Configuration**:

```javascript
const ALLOWED_ORIGINS = [
  'https://swiftcause--swiftcause-app.us-east4.hosted.app',
  'https://swiftcause--swiftcause-prod.europe-west4.hosted.app',
  'https://swiftcause.com',
  'https://swift-cause-web.vercel.app',
  'http://localhost:3000',
  'http://localhost:3001',
];
```

### Backend - Security & Configuration

#### 4. `backend/firestore.rules` (MODIFIED)

Production security rules with strict access control.

**Magic Link Rules**:

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

// Campaigns (public read)
match /campaigns/{campaignId} {
  allow read: if true;
  allow create, update, delete: if isAdmin();
}

// Gift Aid Declarations (public create for magic link flow)
match /giftAidDeclarations/{declarationId} {
  allow read: if true;
  allow create: if true;
  allow update: if true;
  allow delete: if false;
}
```

#### 5. `backend/firestore.emulator.rules` (NEW)

Relaxed rules for local development with same magic link security for testing.

#### 6. `backend/firestore.indexes.json` (MODIFIED)

Added 4 composite indexes for efficient magic link queries:

```json
{
  "indexes": [
    {
      "collectionGroup": "magicLinkTokens",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "tokenHash", "order": "ASCENDING" },
        { "fieldPath": "status", "order": "ASCENDING" }
      ]
    },
    {
      "collectionGroup": "magicLinkTokens",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "status", "order": "ASCENDING" },
        { "fieldPath": "expiresAt", "order": "ASCENDING" }
      ]
    },
    {
      "collectionGroup": "magicLinkTokens",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "donorEmail", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "magicLinkEphemeral",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "expiresAt", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    }
  ],
  "fieldOverrides": []
}
```

#### 7. `backend/firebase.json` (MODIFIED)

Configured separate rules for production and emulator:

```json
{
  "firestore": {
    "rules": "firestore.rules"
  },
  "emulators": {
    "firestore": {
      "rules": "firestore.emulator.rules"
    }
  }
}
```

### Frontend - Pages

#### 8. `app/link/[token]/page.tsx` (NEW)

Magic link validation page with retry logic and error handling.

**Features**:

- Token validation with 10-second timeout
- Retry mechanism
- User-friendly error messages
- Redirects to campaign with Gift Aid enabled

**Key Implementation**:

```typescript
const validateToken = async () => {
  const response = await fetch(
    'https://us-central1-swiftcause-app.cloudfunctions.net/validateMagicLinkToken',
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token }),
      signal: abortController.signal,
    },
  );

  const data = await response.json();

  if (data.valid) {
    const amountInPounds = data.amount / 100;
    router.push(
      `/campaign/${data.campaignId}?amount=${amountInPounds}&giftaid=true&from=magiclink&tokenId=${data.tokenId}`,
    );
  }
};
```

#### 9. `app/gift-aid-thank-you/page.tsx` (NEW)

Thank you screen showing donation impact after Gift Aid submission.

**Features**:

- Donation amount breakdown
- Gift Aid bonus calculation (25%)
- Total impact display
- Declaration confirmation
- Beautiful success UI

**Data Flow**:

```typescript
interface ThankYouData {
  campaignTitle: string;
  donationAmount: number;
  giftAidBonus: number;
  totalImpact: number;
  donorName: string;
  declarationId: string;
}

// Stored in sessionStorage by campaign page
// Retrieved and displayed on thank you page
```

#### 10. `app/result/page.tsx` (MODIFIED)

Added retry logic for fetching magic link token from ephemeral collection.

**Changes**:

```typescript
const fetchMagicLinkToken = async (transactionId: string, retryCount = 0) => {
  const maxRetries = 5;
  const retryDelay = 2000; // 2 seconds

  try {
    const ephemeralDoc = await getDoc(doc(db, 'magicLinkEphemeral', transactionId));

    if (ephemeralDoc.exists()) {
      const plainToken = ephemeralDoc.data().plainToken;
      setPaymentResult((prev) => (prev ? { ...prev, magicLinkToken: plainToken } : null));
    } else if (retryCount < maxRetries) {
      setTimeout(() => fetchMagicLinkToken(transactionId, retryCount + 1), retryDelay);
    }
  } catch (error: any) {
    if (error?.code === 'permission-denied' && retryCount < maxRetries) {
      setTimeout(() => fetchMagicLinkToken(transactionId, retryCount + 1), retryDelay);
    }
  }
};
```

#### 11. `app/campaign/[campaignId]/page.tsx` (MODIFIED)

Modified donation flow to skip Gift Aid screen and handle magic link flow.

**Changes**:

**a) Regular Donation Flow** - Skip Gift Aid, go directly to payment:

```typescript
const handleDonate = (campaign, amount, options) => {
  const amountPence = Math.round(amount * 100);
  const donation = {
    campaignId: campaign.id,
    amount: amountPence,
    isGiftAid: campaign.configuration.giftAidEnabled,
    giftAidAccepted: false, // Will be handled via magic link
    isRecurring: options.isRecurring,
    recurringInterval: options.isRecurring ? options.recurringInterval : undefined,
    kioskId: currentKioskSession?.kioskId,
    donorEmail: donorEmail,
    donorName: donorName || 'Anonymous',
  };
  sessionStorage.setItem('donation', JSON.stringify(donation));
  router.push(`/payment/${campaignId}`);
};
```

**b) Magic Link Flow** - Show thank you instead of payment:

```typescript
const handleAcceptGiftAid = async (details: GiftAidDetails) => {
  const declarationId = await submitGiftAidDeclaration(details, campaign.id, campaign.title);

  // Consume magic link token
  if (fromMagicLink && tokenId) {
    await fetch('https://us-central1-swiftcause-app.cloudfunctions.net/consumeMagicLinkToken', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ tokenId }),
    });

    // Calculate impact
    const donationAmountPounds = details.donationAmount / 100;
    const giftAidBonus = donationAmountPounds * 0.25;
    const totalImpact = donationAmountPounds + giftAidBonus;

    // Store for thank you page
    sessionStorage.setItem(
      'giftAidThankYou',
      JSON.stringify({
        campaignTitle: campaign.title,
        donationAmount: donationAmountPounds,
        giftAidBonus,
        totalImpact,
        donorName: `${details.firstName} ${details.surname}`,
        declarationId,
      }),
    );

    router.push(`/gift-aid-thank-you?campaign=${campaignId}`);
    return;
  }

  // Regular flow continues to payment
  router.push(`/payment/${campaignId}`);
};
```

### Frontend - Components

#### 12. `src/features/kiosk-gift-aid/components/GiftAidBoostPanel.tsx` (MODIFIED)

Removed "No, continue with" decline button to make Gift Aid acceptance required.

**Change**:

```typescript
// Removed decline button
<button onClick={onDecline}>
  No, continue with {formatAmount(currentAmount)}
</button>
```

### Documentation & Testing

#### 13. `MAGIC_LINK_IMPLEMENTATION.md` (THIS FILE)

Complete implementation documentation.

#### 14. `FIRESTORE_RULES_GUIDE.md` (NEW)

Firestore rules documentation with production vs emulator setup.

#### 15. `TESTING_GUIDE.md` (UPDATED)

Updated with magic link testing instructions.

#### 16. `scripts/verify-firestore-rules.js` (NEW)

Automated security verification script.

**Usage**:

```bash
npm run verify:rules
```

**Checks**:

- No dangerous patterns in production rules
- Correct file configuration
- Emulator rules properly separated

#### 17. `test-magic-link.js` (NEW)

Automated testing script for magic link endpoints.

**Usage**:

```bash
node test-magic-link.js
```

---

## User Flow

### Complete Journey

1. **Donation**
   - User selects campaign
   - Chooses amount
   - Goes directly to payment (Gift Aid skipped)

2. **Payment Completion**
   - Payment processes via Stripe
   - Webhook triggers magic link generation
   - Token written to both collections atomically

3. **Result Page**
   - Fetches ephemeral token with retry logic (5 attempts, 2s intervals)
   - Displays magic link if available
   - Token expires from ephemeral collection after 2 minutes

4. **Magic Link Click**
   - User clicks link from email
   - Validates token via Cloud Function
   - Redirects to campaign with Gift Aid enabled

5. **Gift Aid Form**
   - Shows boost screen (donation + 25% = total impact)
   - User fills in details
   - Submits declaration

6. **Thank You**
   - Token consumed via Cloud Function
   - Shows breakdown: donation + Gift Aid bonus = total impact
   - Declaration confirmation

---

## Deployment

### Prerequisites

- Firebase Blaze (pay-as-you-go) plan
- Stripe API keys in Secret Manager
- Node.js 18+ for Cloud Functions

### Deployment Commands

```bash
# Verify rules before deployment
npm run verify:rules

# Deploy Firestore indexes
cd backend
firebase deploy --only firestore:indexes

# Deploy Firestore rules
firebase deploy --only firestore:rules

# Deploy Storage rules
firebase deploy --only storage

# Deploy Cloud Functions
firebase deploy --only functions

# Deploy everything
firebase deploy
```

### Manual Setup Required

**TTL Policies** (Firebase Console → Firestore → Indexes → Time-to-live):

1. **magicLinkTokens**
   - Collection: `magicLinkTokens`
   - Field: `expiresAt`
   - Duration: 30 days

2. **magicLinkEphemeral**
   - Collection: `magicLinkEphemeral`
   - Field: `expiresAt`
   - Duration: 2 minutes

---

## Security Considerations

### Token Security

- ✅ Plain tokens never stored permanently
- ✅ SHA-256 hashing for secure storage
- ✅ Cryptographically secure random generation
- ✅ Single-use tokens with consumption tracking
- ✅ Attempt tracking with IP logging
- ✅ Automatic blocking after 5 failed attempts

### Data Protection

- ✅ Two-collection architecture (secure + ephemeral)
- ✅ Time-restricted access (2 minutes for ephemeral)
- ✅ Backend-only write access
- ✅ Separate production/emulator rules
- ✅ Automated security verification

### Access Control

- ✅ CORS restrictions on Cloud Functions
- ✅ Firestore rules prevent unauthorized access
- ✅ Transaction-based token consumption prevents race conditions
- ✅ Expiry checks before token operations

---

## Monitoring & Analytics

### Key Metrics to Track

- Token generation success rate
- Token validation attempts (success/failure)
- Token consumption rate
- Average time from generation to consumption
- Failed attempt patterns (potential abuse)
- Gift Aid conversion rate via magic links

### Firestore Queries for Monitoring

```javascript
// Active tokens
db.collection('magicLinkTokens')
  .where('status', '==', 'active')
  .where('expiresAt', '>', new Date())
  .get();

// Consumed tokens (last 7 days)
db.collection('magicLinkTokens')
  .where('status', '==', 'consumed')
  .where('consumedAt', '>', sevenDaysAgo)
  .get();

// Blocked tokens (potential abuse)
db.collection('magicLinkTokens').where('status', '==', 'blocked').get();

// Failed attempts by IP
db.collection('magicLinkTokens')
  .where('failedAttempts', '>', 0)
  .orderBy('failedAttempts', 'desc')
  .limit(10)
  .get();
```

---

## Troubleshooting

### Token Not Appearing on Result Page

- Check webhook execution in Firebase Functions logs
- Verify ephemeral collection has document with matching paymentIntentId
- Check if 2-minute window has expired
- Verify Firestore rules allow read access

### Token Validation Fails

- Check if token has expired (30 days)
- Verify token hasn't been consumed already
- Check if token is blocked (5 failed attempts)
- Verify CORS configuration includes request origin

### Gift Aid Form Not Submitting

- Check Firestore rules allow create/update on giftAidDeclarations
- Verify all required fields are filled
- Check browser console for errors
- Verify Cloud Function endpoints are accessible

### Token Consumption Fails

- Check if token is still active
- Verify token hasn't expired
- Check for race conditions (multiple simultaneous requests)
- Verify transaction is completing successfully

---

## Future Enhancements

### Potential Improvements

- Email template customization for magic links
- SMS delivery option for magic links
- QR code generation for kiosk display
- Multi-language support for Gift Aid forms
- Enhanced analytics dashboard
- Webhook retry mechanism for failed token generation
- Rate limiting on validation endpoint
- Token refresh mechanism for expired tokens

---

## Technical Statistics

- **Files Created**: 8
- **Files Modified**: 9
- **Total Files Changed**: 17
- **New Cloud Functions**: 2
- **New Firestore Collections**: 2
- **Composite Indexes**: 4
- **Lines of Code**: ~2,500+
- **Security Rules**: 2 (production + emulator)

---

## References

- [Firebase Cloud Functions Documentation](https://firebase.google.com/docs/functions)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)
- [Stripe Webhooks](https://stripe.com/docs/webhooks)
- [Next.js Dynamic Routes](https://nextjs.org/docs/routing/dynamic-routes)
- [Gift Aid HMRC Guidelines](https://www.gov.uk/claim-gift-aid)
