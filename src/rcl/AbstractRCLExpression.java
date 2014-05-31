package rcl;

public abstract class AbstractRCLExpression implements Iterable<AbstractRCLExpression> {

	public AbstractRCLExpression() {
		super();
	}
	
	public abstract void cleanup();
	
	public abstract int size();
	
	public abstract int numberOfCorrect(AbstractRCLExpression expr);

}