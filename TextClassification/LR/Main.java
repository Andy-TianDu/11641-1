import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws FileNotFoundException {
		HashMap<String, String> params = new HashMap<String, String>();
		// load parameters
		Scanner scanner = new Scanner(new File("DATA.txt"));
		String line;
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();
			String tokens[] = line.split("=");
			params.put(tokens[0], tokens[1]);
		}
		String trainFile = params.get("train");
		String testFile = params.get("test");
		double c = Double.parseDouble(params.get("c"));
		double learning_rate = 0.2;
		Instances trainSet = new Instances(trainFile);
		Instances testSet = new Instances(testFile);
		int num_feature = Math.max(trainSet.numFeature(), testSet.numFeature());
		
		//Logistic Regression for multiclass classification
		MultiClassLR classifier = new MultiClassLR(trainSet.numClass(),
				num_feature, learning_rate, c);
		long startTime = System.currentTimeMillis();
		classifier.train(trainSet);
		long endTime = System.currentTimeMillis();
//		System.out
//				.printf("total time spent:%d\n", (endTime - startTime) / 1000);
		List<Prediction> predictions = classifier.predict(testSet);
		@SuppressWarnings("resource")
		PrintStream ps = System.out;//new PrintStream(new File("result_" + c + ".txt"));
		for (Prediction prediction : predictions)
			ps.println(prediction.predicted);
		
		/* corpus statistics */
//		System.out.println("#corpus statistics#");
//		System.out.printf("Number of instances in the training set:%d\n",
//				trainSet.allInstance.size());
//		System.out.printf("Number of instances in the test set:%d\n",
//				testSet.allInstance.size());
//		System.out.printf("Number of features:%d\n", num_feature);
	}
}
