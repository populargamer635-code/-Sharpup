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

function validProfilePayload(uid, now = new Date(Date.now() - 10000)) {
  return {
    userId: uid,
    childName: "Leo",
    avatarId: "owl",
    difficultyTier: "EXPLORER",
    totalStars: 0,
    brainScore: 100,
    arithmeticSkill: 20,
    speedSkill: 20,
    logicSkill: 20,
    memorySkill: 20,
    currentStreakDays: 0,
    bestStreakCombo: 0,
    sessionsCompleted: 0,
    totalCorrect: 0,
    totalAnswered: 0,
    lastPlayedDate: "",
    unlockedBadges: [],
    createdAt: now,
    updatedAt: now,
  };
}

function validSessionPayload(uid, now = new Date(Date.now() - 2000)) {
  return {
    userId: uid,
    mode: "ARITHMETIC",
    difficultyTier: "EXPLORER",
    score: 180,
    starsEarned: 15,
    correctCount: 9,
    totalQuestions: 10,
    bestCombo: 6,
    durationSeconds: 42,
    createdAt: now,
  };
}

function validLeaderboardPayload(uid, now = new Date(Date.now() - 2000)) {
  return {
    userId: uid,
    childName: "Leo",
    avatarId: "owl",
    difficultyTier: "EXPLORER",
    totalStars: 15,
    brainScore: 165,
    currentStreakDays: 1,
    publicEntry: true,
    updatedAt: now,
  };
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

test("Unauthenticated user cannot read or write profiles, sessions, or leaderboard", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID)));
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("sessions").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("sessions").doc("s1").set(validSessionPayload(ALICE_UID))
  );
  await assertFails(unauthDb.collection("leaderboard").where("publicEntry", "==", true).get());
  await assertFails(unauthDb.collection("leaderboard").doc(ALICE_UID).set(validLeaderboardPayload(ALICE_UID)));
});

test("Authenticated user can create, read, and update own profile via valid actions", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(profileRef.set(validProfilePayload(ALICE_UID)));
  await assertSucceeds(profileRef.get());

  // Action 1: Customize profile
  await assertSucceeds(
    profileRef.update({
      childName: "Super Leo",
      avatarId: "rocket",
      difficultyTier: "CHALLENGER",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Action 2: Record game progress
  await assertSucceeds(
    profileRef.update({
      totalStars: 15,
      brainScore: 165,
      arithmeticSkill: 32,
      speedSkill: 20,
      logicSkill: 20,
      memorySkill: 20,
      currentStreakDays: 1,
      bestStreakCombo: 6,
      sessionsCompleted: 1,
      totalCorrect: 9,
      totalAnswered: 10,
      lastPlayedDate: "2026-11-19",
      unlockedBadges: ["first_spark"],
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Profile rejects shadow fields, future timestamps, immutable mutations, and invalid bounds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);
  await assertSucceeds(profileRef.set(validProfilePayload(ALICE_UID)));

  await assertFails(
    profileRef.update({
      childName: "Leo",
      isAdmin: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertFails(
    profileRef.update({
      childName: "Leo2",
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertFails(
    profileRef.update({
      childName: "Leo3",
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  await assertFails(
    profileRef.update({
      difficultyTier: "ULTRA_HACK",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertFails(
    profileRef.update({
      childName: "Leo4",
      totalStars: 999,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Authenticated user can create and list own game sessions; cannot update or inject @DocumentId id", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const sessionsCol = aliceDb.collection("users").doc(ALICE_UID).collection("sessions");

  await assertSucceeds(sessionsCol.orderBy("createdAt", "desc").limit(20).get());
  await assertSucceeds(sessionsCol.doc("sess_1").set(validSessionPayload(ALICE_UID)));
  await assertSucceeds(sessionsCol.orderBy("createdAt", "desc").limit(20).get());
  await assertFails(sessionsCol.doc("sess_1").update({ score: 9999 }));

  const withId = { ...validSessionPayload(ALICE_UID), id: "sess_2" };
  await assertFails(sessionsCol.doc("sess_2").set(withId));
});

test("Cross-user isolation: Alice cannot read or write Bob's private profile or sessions", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("users").doc(BOB_UID).set(validProfilePayload(BOB_UID));
    await db.collection("users").doc(BOB_UID).collection("sessions").doc("bob_s1").set(validSessionPayload(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).update({ childName: "Hacked", updatedAt: new Date(Date.now() - 1000) }));
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("sessions").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("sessions").doc("bob_s1").get());
});

test("Leaderboard allows filtered public queries, owner updates, and blocks cross-user writes or unfiltered scraping", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(aliceDb.collection("leaderboard").doc(ALICE_UID).set(validLeaderboardPayload(ALICE_UID)));
  await assertSucceeds(bobDb.collection("leaderboard").doc(BOB_UID).set(validLeaderboardPayload(BOB_UID)));

  // Filtered Kotlin query succeeds
  await assertSucceeds(aliceDb.collection("leaderboard").where("publicEntry", "==", true).limit(50).get());

  // Unfiltered query rejected
  await assertFails(aliceDb.collection("leaderboard").get());

  // Owner updates succeed via Action 1 and Action 2
  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).update({
      childName: "Super Leo",
      avatarId: "rocket",
      difficultyTier: "GENIUS",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).update({
      totalStars: 50,
      brainScore: 240,
      currentStreakDays: 3,
      updatedAt: new Date(Date.now() - 300),
    })
  );

  // Cross-user write rejected
  await assertFails(
    aliceDb.collection("leaderboard").doc(BOB_UID).update({
      brainScore: 0,
      totalStars: 0,
      currentStreakDays: 0,
      updatedAt: new Date(Date.now() - 300),
    })
  );
});
