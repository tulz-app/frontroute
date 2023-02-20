package frontroute

trait DirectiveExecute[In] {

  def execute(run: In): Route

}

trait DirectiveUnitExecute {

  def execute(run: => Unit): Route

}
