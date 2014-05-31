package probabilistic;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import com.trainrobots.nlp.scenes.Shape;
import com.trainrobots.nlp.scenes.WorldModel;


public class ProbabilisticTransformer {
	
	private AbstractRealDistribution correctDistribution;
	private AbstractRealDistribution errorDistribution;
	
	public ProbabilisticTransformer(AbstractRealDistribution correctDistribution, AbstractRealDistribution errorDistribution) {
		setCorrectDistribution(correctDistribution);
		setErrorDistribution(errorDistribution);
	}
	
	public ProbabilisticTransformer(AbstractRealDistribution correctDistribution) {
		this(correctDistribution, null);
	}
	
	public AbstractRealDistribution getCorrectDistribution() {
		return correctDistribution;
	}

	private void setCorrectDistribution(AbstractRealDistribution distribution) {
		this.correctDistribution = distribution;
	}

	public AbstractRealDistribution getErrorDistribution() {
		return errorDistribution;
	}

	private void setErrorDistribution(AbstractRealDistribution errorDistribution) {
		this.errorDistribution = errorDistribution;
	}

	public ProbabilisticBelief generateBeliefFromWorld(WorldModel world) {
		List<ProbabilisticItem> items = new ArrayList<ProbabilisticItem>();
		String gripperObject = null;
		for (Shape shape : world.shapes()) {
			int i = items.size() + 1;
			items.addAll(generateItemsForShape(shape, i));
			
			if (world.getShapeInGripper() != null && world.getShapeInGripper().equals(shape)) {
				gripperObject = "o"+i;
			}
		}
		
		return new ProbabilisticBelief(items, gripperObject);
	}
	
	private final static String colors[] = {"red", "green", "blue", "magenta", "cyan", "white", "yellow", "gray"};
	private final static String types[] = {"cube", "prism"};
	public static double probabilityThreshold = 0.25;
	
	public List<ProbabilisticItem> generateItemsForShape(Shape shape, int i) {
		List<ProbabilisticItem> result = new ArrayList<ProbabilisticItem>();
		
		Random rand = new Random();
		
		ProbabilisticItem item = new ProbabilisticItem();
		double prob = correctDistribution.sample();
		if (prob > 1) {
			prob = 1;
		} else if (prob < 0) {
			prob = 0;
		}
		item.probability = prob;
		item.variable = "o"+i;
		item.color = shape.color().toString();
		item.type = shape.type().toString();
		item.x = shape.position().x;
		item.y = shape.position().y;
		item.z = shape.position().z;
		result.add(item);
		
		if (errorDistribution != null) {
			for (int j = 0; j < colors.length; j++) {
				for (int k = 0; k < types.length; k++) {
					if (!colors[j].equals(item.color) || !types[k].equals(item.type)) {
						if (rand.nextDouble() < probabilityThreshold) {
							ProbabilisticItem it = new ProbabilisticItem();
							prob = errorDistribution.sample();
							if (prob > 1) {
								prob = 1;
							} else if (prob < 0) {
								prob = 0;
							}
							if (prob > 0) {
								it.probability = prob;
								it.variable = "o"+(i + result.size());
								it.color = colors[j];
								it.type = types[k];
								it.x = shape.position().x;
								it.y = shape.position().y;
								it.z = shape.position().z;
								result.add(it);
							}
						}
					}
				}
			}
		}
		
		return result;
	}

}
