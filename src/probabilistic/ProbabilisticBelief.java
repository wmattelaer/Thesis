package probabilistic;
import java.util.List;


public class ProbabilisticBelief {
	
	private List<ProbabilisticItem> items;
	private String gripperObject;
	
	public ProbabilisticBelief(List<ProbabilisticItem> items) {
		this(items, null);
	}
	
	public ProbabilisticBelief(List<ProbabilisticItem> items, String gripperObject) {
		setItems(items);
		setGripperObject(gripperObject);
	}
	
	public List<ProbabilisticItem> getItems() {
		return items;
	}

	private void setItems(List<ProbabilisticItem> items) {
		this.items = items;
	}
	
	public String getGripperObject() {
		return gripperObject;
	}

	private void setGripperObject(String gripperObject) {
		this.gripperObject = gripperObject;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (ProbabilisticItem item : getItems()) {
			if (builder.length() > 0) {
				builder.append("\n");
			}
			builder.append(item.toString());
		}
		if (gripperObject != null) {
			builder.append("\n");
			builder.append("gripper("+gripperObject+").");
		}
		
		
		return builder.toString();
	}

}
