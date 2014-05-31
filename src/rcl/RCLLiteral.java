package rcl;
import java.util.Iterator;


public class RCLLiteral extends AbstractRCLExpression {

	private String value;
	
	public RCLLiteral(String value) {
		setValue(value);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof RCLLiteral) {
			RCLLiteral literal = (RCLLiteral) object;
			return literal.getValue().equals(getValue());
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	@Override
	public Iterator<AbstractRCLExpression> iterator() {
		final AbstractRCLExpression thisObject = this;
		
		return new Iterator<AbstractRCLExpression>() {
			
			private boolean hasReturnedSelf = false;

			@Override
			public boolean hasNext() {
				return !hasReturnedSelf;
			}

			@Override
			public AbstractRCLExpression next() {
				hasReturnedSelf = true;
				return thisObject;
			}

			@Override
			public void remove() {
				
			}
		};
	}

	@Override
	public void cleanup() {
		
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public int numberOfCorrect(AbstractRCLExpression expr) {
		if (equals(expr)) {
			return 1;
		} else {
			return 0;
		}
	}
	
}
