// tests/tokenBudget.spec.ts
import { describe, it, expect } from "bun:test";
import { TokenBudget } from "../domain/TokenBudget";

describe("TokenBudget", () => {
  it("allows_spending_within_the_max", () => {
    const b = new TokenBudget(10, 0);

    let used = b.getUsed();
    let max = b.getMax();
    console.log("___used___: " + used);
    console.log("___max___: " + max);

    b.spend(4);
    used = b.getUsed();
    console.log("___used___: " + used);
    console.log("___max___: " + max);

    expect(used).toBe(4);

    expect(b.isMaxedOut()).toBe(true);
  });

  /*it("throws_when_exceeding_max", () => {
    const b = new TokenBudget(5, 0);
    expect(() => b.spend(6)).toThrow("BudgetExceeded");
  });*/
});