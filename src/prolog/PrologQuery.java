package prolog;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PrologQuery {
	
	private List<PrologPredicate> predicates;
	
	public PrologQuery(List<PrologPredicate> predicates) {
		setPredicates(predicates);
	}
	
	public List<PrologPredicate> getPredicates() {
		return predicates;
	}

	private void setPredicates(List<PrologPredicate> predicates) {
		this.predicates = predicates;
	}
	
	public boolean moreGeneralThan(PrologQuery query) {
		Map<String, String> variableMap = new HashMap<String, String>();
		for (PrologPredicate p1 : getPredicates()) {
			boolean found = false;
			for (PrologPredicate p2 : query.getPredicates()) {
				if (p1.equals(p2, variableMap)) {
					found = true;
					break;
				}
			}
			
			if (!found) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < predicates.size(); i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append(predicates.get(i));
		}
		builder.append(".");
		
		return builder.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof PrologQuery) {
			return predicates.equals(((PrologQuery) o).getPredicates());
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return predicates.hashCode();
	}

}
