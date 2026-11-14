const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";
const ADMIN_UID = "admin_uid_999";
const ADMIN_EMAIL = "nafeessaudagar7@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function serverTime(testEnvInstance) {
  return new Date();
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// 1. Unauthenticated access rejected
test("Unauthenticated user cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("requests").get());
  await assertFails(unauthDb.collection("transactions").get());
  await assertFails(unauthDb.collection("notifications").get());
  await assertFails(unauthDb.collection("support_tickets").get());
});

// 2. User signup & self-profile protection
test("User can create own standard profile, cannot escalate role/status/balance or add ghost fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();

  const validProfile = {
    userId: ALICE_UID,
    fullName: "Alice Sharma",
    email: "alice@example.com",
    phone: "+919876543210",
    role: "user",
    status: "active",
    accessLevel: "standard",
    otpVerified: false,
    pinHash: "sha256:salt:digest",
    walletBalance: 0,
    createdAt: new Date(),
  };

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validProfile));

  // Cannot escalate role on create
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({ ...validProfile, role: "admin" })
  );

  // Cannot inject ghost field (Shadow Update Test)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({ isVerified: true })
  );

  // Cannot self-modify walletBalance or status
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({ walletBalance: 50000 })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({ role: "admin" })
  );

  // Can update own allowed profile fields
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      fullName: "Alice S.",
      otpVerified: true,
      updatedAt: new Date(),
    })
  );
});

// 3. Cross-user PII isolation
test("User cannot read or list another user's profile or requests", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      fullName: "Bob Verma",
      email: "bob@example.com",
      phone: "+919999999999",
      role: "user",
      status: "active",
      accessLevel: "standard",
      otpVerified: true,
      pinHash: "hash",
      walletBalance: 100,
      createdAt: new Date(),
    });
    await db.collection("requests").doc("req_bob_1").set({
      userId: BOB_UID,
      userName: "Bob Verma",
      userEmail: "bob@example.com",
      type: "deposit",
      title: "Deposit Funds",
      description: "UPI ref 123",
      amount: 1000,
      status: "pending",
      adminNote: "",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();

  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("users").get());
  await assertFails(aliceDb.collection("requests").doc("req_bob_1").get());
  await assertFails(aliceDb.collection("requests").get());
});

// 4. Email spoofing test for Admin access
test("Unverified email spoofing admin email is rejected", async () => {
  const spoofDb = testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();

  await assertFails(spoofDb.collection("users").get());
  await assertFails(spoofDb.collection("requests").get());
});

// 5. Bootstrapped admin & User-wise operations
test("Verified admin can list users, activate/deactivate users, approve requests, and create transactions", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice Sharma",
      email: "alice@example.com",
      phone: "9876543210",
      role: "user",
      status: "active",
      accessLevel: "standard",
      otpVerified: true,
      pinHash: "hash",
      walletBalance: 0,
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();

  // Alice creates a pending deposit request
  await assertSucceeds(
    aliceDb.collection("requests").doc("req_alice_1").set({
      userId: ALICE_UID,
      userName: "Alice Sharma",
      userEmail: "alice@example.com",
      type: "deposit",
      title: "Deposit Credits",
      description: "IMPS #9910",
      amount: 2500,
      status: "pending",
      adminNote: "",
      createdAt: new Date(),
    })
  );

  // Alice can query her own requests
  await assertSucceeds(
    aliceDb.collection("requests").where("userId", "==", ALICE_UID).get()
  );

  // Alice cannot approve her own request or create transactions
  await assertFails(
    aliceDb.collection("requests").doc("req_alice_1").update({
      status: "approved",
      adminNote: "Self approved",
      updatedAt: new Date(),
    })
  );
  await assertFails(
    aliceDb.collection("transactions").doc("tx_1").set({
      userId: ALICE_UID,
      userName: "Alice Sharma",
      referenceCode: "TXN-1001",
      type: "credit",
      category: "Deposit",
      amount: 2500,
      status: "completed",
      note: "Hack",
      createdByUid: ALICE_UID,
      createdAt: new Date(),
    })
  );

  // Verified Admin can manage Alice, approve request, write immutable transaction, and send notification
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  await assertSucceeds(adminDb.collection("users").get());
  await assertSucceeds(
    adminDb.collection("users").doc(ALICE_UID).update({
      status: "inactive",
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(
    adminDb.collection("requests").doc("req_alice_1").update({
      status: "approved",
      adminNote: "Verified and credited",
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(
    adminDb.collection("transactions").doc("tx_1").set({
      userId: ALICE_UID,
      userName: "Alice Sharma",
      referenceCode: "MHD-TXN-1001",
      type: "credit",
      category: "Deposit",
      amount: 2500,
      status: "completed",
      note: "Deposit approved",
      createdByUid: ADMIN_UID,
      createdAt: new Date(),
    })
  );

  // Transactions are immutable (update/delete fail even for admin)
  await assertFails(
    adminDb.collection("transactions").doc("tx_1").update({ amount: 9999 })
  );
  await assertFails(adminDb.collection("transactions").doc("tx_1").delete());

  // Admin creates notification for Alice; Alice can mark isRead = true
  await assertSucceeds(
    adminDb.collection("notifications").doc("notif_1").set({
      userId: ALICE_UID,
      title: "Request Approved",
      message: "Your deposit of 2500 was approved.",
      category: "request",
      isRead: false,
      createdAt: new Date(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("notifications").doc("notif_1").update({ isRead: true })
  );
});

// 6. Support Tickets
test("User can submit support ticket, admin can reply and resolve", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    aliceDb.collection("support_tickets").doc("tkt_1").set({
      userId: ALICE_UID,
      userName: "Alice Sharma",
      subject: "Account Reactivation Help",
      message: "Please review my account activation status.",
      priority: "high",
      status: "open",
      adminReply: "",
      createdAt: new Date(),
    })
  );

  // User cannot resolve own ticket
  await assertFails(
    aliceDb.collection("support_tickets").doc("tkt_1").update({
      status: "resolved",
      adminReply: "Done",
      updatedAt: new Date(),
    })
  );

  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    adminDb.collection("support_tickets").doc("tkt_1").update({
      status: "resolved",
      adminReply: "Account reviewed and reactivated.",
      updatedAt: new Date(),
    })
  );
});
