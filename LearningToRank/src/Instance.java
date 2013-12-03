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
	
	/**
	 * normalize feature vector using l2-norm
	 */
	public void normalize() {
		double norm = 0;
		for (double value : feature.values()) {
			norm += value * value;
		}
		norm = Math.sqrt(norm);
		for (int fId : feature.keySet()) {
			double value = feature.get(fId);
			feature.put(fId, value / norm);
		}
	}

	public String toString() {
		StringBuffer str = new StringBuffer(String.format("%d ", label));
		for (int id : feature.keySet())
			str.append(String.format("%d:%f ", id, feature.get(id)));
		return str.toString();
	}
}
