package rcl;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class RCLExpression extends AbstractRCLExpression {
	
	private String type;
	
	private List<AbstractRCLExpression> subExpressions;
	
	public RCLExpression(String type, List<AbstractRCLExpression> subExpressions) {
		setType(type);
		setSubExpressions(subExpressions);
	}
	
	public RCLExpression(String type) {
		setType(type);
		setSubExpressions(new ArrayList<AbstractRCLExpression>());
	}
	
	public RCLExpression(String type, AbstractRCLExpression subExpression) {
		setType(type);
		List<AbstractRCLExpression> temp = new ArrayList<AbstractRCLExpression>();
		temp.add(subExpression);
		setSubExpressions(temp);
	}

	public String getType() {
		return type;
	}

	private void setType(String type) {
		this.type = type;
	}

	public List<AbstractRCLExpression> getSubExpressions() {
		return subExpressions;
	}

	private void setSubExpressions(List<AbstractRCLExpression> subExpressions) {
		this.subExpressions = subExpressions;
	}
	
	public RCLExpression getSubExpressionWithType(String type) {
		for (AbstractRCLExpression expr : getSubExpressions()) {
			if (expr instanceof RCLExpression) {
				if (((RCLExpression) expr).getType().equals(type)) {
					return (RCLExpression) expr;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof RCLExpression)) {
			return false;
		}
		
		RCLExpression expr = (RCLExpression) object;
		if (!expr.getType().equals(getType())) {
			return false;
		}
		for (AbstractRCLExpression e : subExpressions) {
			if (!expr.getSubExpressions().contains(e)) {
				return false;
			}
		}
		for (AbstractRCLExpression e : expr.subExpressions) {
			if (!getSubExpressions().contains(e)) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('(');
		builder.append(type);
		if (subExpressions.size() > 0) {
			builder.append(": ");
			int i = 0;
			for (AbstractRCLExpression expr : subExpressions) {
				if (i++ > 0) {
					builder.append(" ");
				}
				builder.append(expr);
			}
		}
		builder.append(')');
		
		return builder.toString();
	}
	
	@Override
	public Iterator<AbstractRCLExpression> iterator() {
		final AbstractRCLExpression thisObject = this;
		
		return new Iterator<AbstractRCLExpression>() {

			private int currentExpression = -1;
			private Iterator<AbstractRCLExpression> currentIterator;
			private boolean hasReturnedSelf = false;
			
			@Override
			public boolean hasNext() {
				if (currentIterator != null && currentIterator.hasNext()) {
					return true;
				} else {
					if (currentExpression < getSubExpressions().size() - 1) {
						return true;
					} else if (!hasReturnedSelf) {
						return true;
					} else {
						return false;
					}
				}
			}

			@Override
			public AbstractRCLExpression next() {
				if (currentIterator != null && currentIterator.hasNext()) {
					return currentIterator.next();
				} else {
					if (!hasReturnedSelf) {
						hasReturnedSelf = true;
						return thisObject;
					} else if (currentExpression < getSubExpressions().size() - 1) {
						currentExpression++;
						currentIterator = getSubExpressions().get(currentExpression).iterator();
						return currentIterator.next();
					} else {
						return null;
					}
				}
			}

			@Override
			public void remove() {
				// TODO Auto-generated method stub
				
			}
		};
	}

	@Override
	public void cleanup() {
		for (int i = 0; i < this.subExpressions.size(); i++) {
			if (this.subExpressions.get(i) != null) {
				this.subExpressions.get(i).cleanup();
			}
		}
		
		// Remove duplicate elements
		for (int i = 0; i < this.subExpressions.size(); i++) {
			if (this.subExpressions.get(i) != null) {
				for (int j = i + 1; j < this.subExpressions.size(); j++) {
					if (this.subExpressions.get(j) != null && this.subExpressions.get(i).equals(this.subExpressions.get(j))) {
						this.subExpressions.remove(i);
						i--;
						break;
					}
				}
			}
		}
		
	}
	
	public void removeUnusedIds() {
		try {
			Set<Integer> usedIds = new HashSet<Integer>();
			for (AbstractRCLExpression expr : this) {
				if (expr instanceof RCLExpression && ((RCLExpression) expr).getType().equals("reference-id")) {
					usedIds.add(Integer.parseInt(((RCLLiteral) ((RCLExpression) expr).getSubExpressions().get(0)).getValue()));
				}
			}
			
			for (AbstractRCLExpression expr : this) {
				if (expr instanceof RCLExpression) {
					RCLExpression e = (RCLExpression) expr;
					for (int i = 0; i < e.getSubExpressions().size(); i++) {
						if (e.getSubExpressions().get(i) instanceof RCLExpression && ((RCLExpression) e.getSubExpressions().get(i)).getType().equals("id")) {
							int val = Integer.parseInt(((RCLLiteral) ((RCLExpression) e.getSubExpressions().get(i)).getSubExpressions().get(0)).getValue());
							if (!usedIds.contains(val)) {
								e.getSubExpressions().remove(i);
								i--;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			
		}
	}

	@Override
	public int size() {
		int result = 1;
		for (AbstractRCLExpression expr : this.getSubExpressions()) {
			result += expr.size();
		}
		
		return result;
	}

	@Override
	public int numberOfCorrect(AbstractRCLExpression expr) {
		if (expr instanceof RCLExpression) {
			RCLExpression expression = (RCLExpression) expr;
			int temp = 0;
			
			if (expression.equals(this)) {
				temp++;
			}
			
			for (AbstractRCLExpression e : getSubExpressions()) {
				if (e instanceof RCLLiteral) {
					if (expression.getSubExpressions().contains(e)) {
						temp++;
					}
				} else {
					String type = ((RCLExpression) e).getType();
					int best = 0;
					for (AbstractRCLExpression e3 : expression.getSubExpressions()) {
						if (e3 instanceof RCLExpression && ((RCLExpression) e3).getType().equals(type)) {
							int n = e.numberOfCorrect(e3);
							if (n > best) {
								best = n;
							}
						}
					}
					temp += best;
				}
			}
			
			return temp;
		} else {
			return 0;
		}
	}

}
