const test = require("node:test");
const assert = require("node:assert");
const { codeProblem, tokensOf, deadTokens } = require("../logic");

test("a fresh code from someone else works", () => {
  assert.strictEqual(codeProblem({ expiresAt: 2000, createdBy: "kid" }, 1000, "mom"), null);
});

test("missing, expired and own codes are refused", () => {
  assert.strictEqual(codeProblem(null, 1000, "mom"), "not-found");
  assert.strictEqual(codeProblem({ expiresAt: 500, createdBy: "kid" }, 1000, "mom"), "deadline-exceeded");
  assert.strictEqual(codeProblem({ expiresAt: 2000, createdBy: "kid" }, 1000, "kid"), "failed-precondition");
});

test("tokens are collected once each", () => {
  assert.deepStrictEqual(tokensOf([{ tokens: ["a", "b"] }, null, { tokens: ["b", "", 3, "c"] }, {}]), ["a", "b", "c"]);
});

test("only unregistered tokens are dropped", () => {
  const responses = [
    { success: true },
    { error: { code: "messaging/registration-token-not-registered" } },
    { error: { code: "messaging/internal-error" } },
    { error: { code: "messaging/invalid-registration-token" } },
  ];
  assert.deepStrictEqual(deadTokens(["a", "b", "c", "d"], responses), ["b", "d"]);
});
