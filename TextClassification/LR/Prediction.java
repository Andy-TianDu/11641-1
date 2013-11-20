/**
 * Prediction - 
 *  stores predicted label and ground truth label for each instance
 * 
 * @author Siping Ji <sipingji@cmu.edu>
 * 
 */
public class Prediction {
	int predicted;
	int groundTruth;

	public Prediction(int predicted, int groundTruth) {
		this.predicted = predicted;
		this.groundTruth = groundTruth;
	}

	public String toString() {
		return String.format("%d %d", predicted, groundTruth);
	}
}
