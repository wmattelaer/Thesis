package prolog;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PrologPredicate {
	
	private String type;
	private List<String> values;
	
	public PrologPredicate(String type, List<String> values) {
		setType(type);
		setValues(values);
	}

	public String getType() {
		return type;
	}

	private void setType(String type) {
		this.type = type;
	}

	public List<String> getValues() {
		return values;
	}

	private void setValues(List<String> values) {
		this.values = values;
	}
	
	boolean equals(PrologPredicate predicate, Map<String, String> variableMap) {
		if (!type.equals(predicate.getType()) || values.size() != predicate.getValues().size()) {
			return false;
		}
		
		for (int i = 0; i < getValues().size(); i++) {
			String val1 = values.get(i);
			String val2 = predicate.getValues().get(i);
			if (Character.isUpperCase(val1.charAt(0))) {
				if (Character.isUpperCase(val2.charAt(0))) {
					if (!variableMap.keySet().contains(val1) && !variableMap.values().contains(val2)) {
						variableMap.put(val1, val2);
					} else if (variableMap.get(val1).equals(val2)) {
						
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else if (Character.isUpperCase(val2.charAt(0))) {
				return false;
			} else {
				if (!val1.equals(val2)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof PrologPredicate) {
			PrologPredicate p = (PrologPredicate) o;
			
			return equals(p, new HashMap<String, String>());
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(type);
		builder.append("(");
		for (int i = 0; i < values.size(); i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append(values.get(i));
		}
		builder.append(")");
		
		return builder.toString();
	}

}
