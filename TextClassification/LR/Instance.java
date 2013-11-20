import java.util.HashMap;

/**
 * Instance
 * @author Siping Ji <sipingji@cmu.edu>
 *
 */
public class Instance {
	public int label;
	public HashMap<Integer, Double> feature = new HashMap<Integer, Double>();

	public Instance(int label) {
		this.label = label;
	}

	/**
	 * get binary label
	 * @param trueLabel
	 * @return
	 */
	public int getLabel(int trueLabel) {
		return trueLabel == label ? 1 : 0;
	}

	/**
	 * get original label
	 * @return
	 */
	public int getLabel() {
		return label;
	}

	public void addFeature(int fid, double value) {
		feature.put(fid, value);
	}

	public String toString() {
		StringBuffer str = new StringBuffer(String.format("%d ", label));
		for (int id : feature.keySet())
			str.append(String.format("%d:%f ", id, feature.get(id)));
		str.append("\n");
		return str.toString();
	}
}
