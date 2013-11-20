import java.util.ArrayList;
import java.util.List;

/**
 * Multi-class Logistic Regression using one-vs-rest strategy
 * 
 * @author Siping Ji <sipingji@cmu.edu>
 * 
 */
public class MultiClassLR {
	private int numClass;
	private List<LogisticRegression> models = new ArrayList<LogisticRegression>();

	public MultiClassLR(int numClass, int numFeature, double learning_rate,
			double C) {
		this.numClass = numClass;
		for (int i = 1; i <= numClass; i++) {
			models.add(new LogisticRegression(numFeature, learning_rate, C, i));
		}
	}

	/**
	 * train numClass models of binary logistic regression
	 * 
	 * @param trainSet
	 */
	public void train(Instances trainSet) {
		int i = 1;
		for (LogisticRegression model : models) {
//			System.out.printf("train model %d\n", i++);
			model.train(trainSet);
		}
	}

	/**
	 * multi-class classification using one-vs-all strategy
	 * 
	 * @param testSet
	 * @return
	 */
	public List<Prediction> predict(Instances testSet) {
		List<Prediction> predictions = new ArrayList<Prediction>();
		for (Instance instance : testSet.allInstance) {
			double max_prob = 0;
			int predictLabel = 1;
			for (LogisticRegression model : models) {
				double pred_prob = model.predict(instance);
				if (pred_prob > max_prob) {
					max_prob = pred_prob;
					predictLabel = model.getTrueLabel();
				}
			}
			Prediction prediction = new Prediction(predictLabel,
					instance.getLabel());
			predictions.add(prediction);
		}
		return predictions;
	}

}
