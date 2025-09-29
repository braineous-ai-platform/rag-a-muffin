
// src/domain/TokenBudget.ts
export class TokenBudget {
  constructor(public max: number, public used = 0) {}

  canSpend(n: number) { return this.used + n <= this.max; }

  spend(n: number) {
    if (!this.canSpend(n)) throw new Error("BudgetExceeded");
    this.used += n;
  }
}

// run
console.log("____token_budget_2____");