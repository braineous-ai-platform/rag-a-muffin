export class TokenBudget {
  private max:number = 0;
  private used:number = 0;

  constructor(max: number, used: number) {
    this.max = max;
    this.used = used;
  }

  public canSpend(n: number) : number{ 
    if(n <= this.max){
      this.used += n;

      console.log("canSpend________________" +this.used);
      return this.used;
    }

    throw new Error("BudgetExceeded");
  }

  public spend(n: number) : void{
    this.used += n;

    console.log("spend(used)________________" +this.used);
    console.log("spend(max)________________" +this.used);
  }

  public isMaxedOut(): boolean {
    if(this.used > this.max){
      //maxed_out
      return false;
    }
    return true;

    //return true;
  }

  //getter_and_setter
  public getUsed(): number {
    console.log("getUsed________________" +this.used);
    return this.used;
  }

  public getMax(): number {
    return this.max;
  }

}