package probabilistic;

public class ProbabilisticItem {
	
	double probability;
	String variable;
	String color;
	String type;
	int x;
	int y;
	int z;
	
	@Override
	public String toString() {
		return probability + "::object(" + variable + ", " + color + ", " + type + ", " + x + ", " + y + ", " + z + ").";
	}

}
