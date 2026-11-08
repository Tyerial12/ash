const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

test("Unauthenticated user cannot read or list public_vaults", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("public_vaults").where("visibility", "==", "public").get());
});

test("Authenticated user can create and update their own public_vaults profile, but not another user's", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("public_vaults").doc(ALICE_UID).set({
      userId: ALICE_UID,
      overseerName: "Overseer Alice",
      vaultName: "Vault 101",
      visibility: "public",
      prestigeLevel: 2,
      commandLevel: 14,
      dayCounter: 28,
      totalSurvivors: 12,
      uniqueDwellersCount: 2,
      totalDefenseRating: 340,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob cannot overwrite Alice's vault
  await assertFails(
    bobDb.collection("public_vaults").doc(ALICE_UID).update({
      vaultName: "Hacked Vault",
      updatedAt: now,
    })
  );

  // Bob can query public vaults with visibility == public
  await assertSucceeds(
    bobDb.collection("public_vaults").where("visibility", "==", "public").get()
  );

  // Shadow update with ghost field must fail
  await assertFails(
    aliceDb.collection("public_vaults").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: now,
    })
  );
});

test("Public supply drops: Alice can post a drop, Bob can claim it once, and terminal state prevents re-claiming", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("public_supply_drops").doc("drop_1").set({
      senderUid: ALICE_UID,
      senderName: "Overseer Alice",
      resourceType: "RATIONS",
      amount: 30,
      message: "Extra hydroponic rations for nearby Vaults!",
      visibility: "public",
      status: "open",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob claims the open supply drop
  await assertSucceeds(
    bobDb.collection("public_supply_drops").doc("drop_1").update({
      status: "claimed",
      claimedByUid: BOB_UID,
      claimedByName: "Overseer Bob",
      updatedAt: now,
    })
  );

  // Once claimed (terminal state), Alice or Bob cannot re-claim or mutate it
  await assertFails(
    aliceDb.collection("public_supply_drops").doc("drop_1").update({
      status: "claimed",
      claimedByUid: ALICE_UID,
      claimedByName: "Overseer Alice",
      updatedAt: now,
    })
  );
});
