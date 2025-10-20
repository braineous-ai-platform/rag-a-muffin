"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.TokenBudget = void 0;
var TokenBudget = /** @class */ (function () {
    function TokenBudget(max, used) {
        this.max = 0;
        this.used = 0;
        this.max = max;
        this.used = used;
    }
    TokenBudget.prototype.canSpend = function (n) {
        if (n <= this.max) {
            this.used += n;
            return this.used;
        }
        throw new Error("BudgetExceeded");
    };
    TokenBudget.prototype.spend = function (n) {
        this.canSpend(n);
        this.used += n;
    };
    //getter_and_setter
    TokenBudget.prototype.getUsed = function () {
        return this.used;
    };
    return TokenBudget;
}());
exports.TokenBudget = TokenBudget;
