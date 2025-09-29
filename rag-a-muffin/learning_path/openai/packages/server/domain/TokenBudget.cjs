"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.TokenBudget = void 0;
// src/domain/TokenBudget.ts
var TokenBudget = /** @class */ (function () {
    function TokenBudget(max, used) {
        if (used === void 0) { used = 0; }
        this.max = max;
        this.used = used;
    }
    TokenBudget.prototype.canSpend = function (n) { return this.used + n <= this.max; };
    TokenBudget.prototype.spend = function (n) {
        if (!this.canSpend(n))
            throw new Error("BudgetExceeded");
        this.used += n;
    };
    return TokenBudget;
}());
exports.TokenBudget = TokenBudget;
// run
console.log("____token_budget_2____");
