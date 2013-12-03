
/**
 * Instance
 * @author Siping Ji <sipingji@cmu.edu>
 *
 */
public class Instance {
	public int label;
	public double features[];

	public Instance(int label, int numFeature) {
		this.label = label;
		features = new double[numFeature];
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
		features[fid] = value;
	}
	
	/**
	 * normalize feature vector using l2-norm
	 */
	public void normalize() {
		double norm = 0;
		for (double feature : features) {
			norm += feature * feature;
		}
		norm = Math.sqrt(norm);
		for (int i = 0; i < features.length; i++) {
			features[i] /= norm;
		}
	}

	public String toString() {
		StringBuffer str = new StringBuffer(String.format("%d ", label));
		for (int i = 0; i < features.length; i++)
			str.append(String.format("%d:%f ", i, features[i]));
		return str.toString();
	}
}
