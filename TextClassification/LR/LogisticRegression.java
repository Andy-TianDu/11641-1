import java.util.HashMap;

/**
 * Logistic Regression - perform binary classification, output the probability
 * that an instance belongs to a class
 * 
 * @author Siping Ji <sipingji@cmu.edu>
 * 
 */
public class LogisticRegression {
	private double weights[];

	private double learning_rate = 0.2;

	private double C = 0;

	private static final int MAX_ITERATION = 100;

	private static final double epsilon = 1e-3;

	private int trueLabel = 1; // the label this binary classifier count as
								// positive

	public LogisticRegression(int numFeature, double learning_rate, double C,
			int trueLabel) {
		this.learning_rate = learning_rate;
		this.C = C;
		this.trueLabel = trueLabel;
		weights = new double[numFeature];
		// varying learning rate for different value C
		if (C >= 10)
			this.learning_rate = 1e-5;
		else if (C >= 1)
			this.learning_rate = 1e-3;
		else if (C >= 0.1)
			this.learning_rate = 0.005;
		else
			this.learning_rate = 0.02;
//		System.out.printf("alpha:%f, C:%f trueLabel:%d\n", learning_rate, C,
//				trueLabel);
	}

	private double sigmoid(double z) {
		return 1 / (1 + Math.exp(-z));
	}

	private double dotProduct(double[] w, HashMap<Integer, Double> feature) {
		double product = 0;
		for (int key : feature.keySet()) {
			product += feature.get(key) * w[key];
		}
		return product;
	}

	/**
	 * make soft prediction output probability that an instance belongs to
	 * trueLabel class
	 * 
	 * @param instance
	 * @return
	 */
	public double predict(Instance instance) {
		double prob = sigmoid(dotProduct(weights, instance.feature));
		return prob;
	}

	private double logLikelihood(Instances instances) {
		double likelihood = 0;
		for (int i = 0; i < instances.numInstance(); i++) {
			Instance instance = instances.getInstance(i);
			int y = instance.getLabel(trueLabel);
			double prob = predict(instance);
			likelihood += y * Math.log(prob) + (1 - y) * Math.log(1 - prob);
		}

		for (int i = 1; i < weights.length; i++) {
			likelihood -= 1.0 / 2 * C * weights[i] * weights[i];
		}
		return likelihood;
	}

	/**
	 * update weights using stochastic gradient descent
	 * 
	 * @param instance
	 */
	private void updateWeights(Instance instance) {
		int y = instance.getLabel(trueLabel);
		double prediction = predict(instance);
		// first iterate through all the weights
		// update the derivatie of regularization term
		for (int i = 1; i < weights.length; i++) {
			weights[i] -= learning_rate * C * weights[i];
		}
		// update derivative of likelihood function
		for (int key : instance.feature.keySet()) {
//			if (key != 0)
				weights[key] += learning_rate * (y - prediction)
						* instance.feature.get(key);
		}
	}

	/**
	 * train the model using stochastic gradient descent
	 * 
	 * @param trainSet
	 */
	public void train(Instances trainSet) {
//		int numPos = trainSet.numPos(trueLabel);
//		weights[0] = 1.0 * numPos / (trainSet.numInstance() - numPos);
		double ll = logLikelihood(trainSet);
		for (int i = 0; i < MAX_ITERATION; i++) {
//			System.out.printf("iteration %d\n", i + 1);
			trainSet.shuffle();
			for (Instance instance : trainSet.allInstance) {
				updateWeights(instance);
			}
			double tmp_ll = logLikelihood(trainSet);
			if (Math.abs(tmp_ll - ll) < epsilon)
				break;
			ll = tmp_ll;
//			System.out.printf("log likelood%f\n", ll);
		}
	}

	public void setTrueLabel(int trueLabel) {
		this.trueLabel = trueLabel;
	}

	public int getTrueLabel() {
		return trueLabel;
	}

}