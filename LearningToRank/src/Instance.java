
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
	
	public void addCustomFeature() {
		// first custom feature
		// BM25 * (hostrank + pagerank + hub score + authority score)
//		this.addFeature(44, features[0] * (features[5] + features[6] + features[7] + features[13]));
//		// BM25 of extracted title * BM25 of anchor
//		this.addFeature(45, features[15] * features[19]);
//		// BM25 normalized by document length
//		this.addFeature(46, features[0] / (features[1] + features[2] + features[3] + features[4] + 1));
	}

	public String toString() {
		StringBuffer str = new StringBuffer(String.format("%d ", label));
		for (int i = 0; i < features.length; i++)
			str.append(String.format("%d:%f ", i, features[i]));
		return str.toString();
	}
}
