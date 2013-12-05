import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;


public class LearningToRank {
	
	private static int numFeature = 44;

	public static void main(String args[]) throws FileNotFoundException {
		//read parameters
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
		double learning_rate = 0.01;
		Instances trainSet = formTrainSet(trainFile);
		Instances testSet = formTestSet(testFile);
		/* 
		 * normalize feature vector for faster convergence
		 * also normalization reduces bias caused by diffrente scales of features
		 */ 
		trainSet.normalize();
		testSet.normalize();
		LogisticRegression lr = new LogisticRegression(Math.max(
				trainSet.numFeature(), testSet.numFeature()), learning_rate, c,
				1);
		long start = System.currentTimeMillis();
		lr.train(trainSet);
//		System.out.println(lr);
		long duration = System.currentTimeMillis() - start;
		System.out.printf("Training Time: %f s\n", (1.0 * duration / 1000));

		@SuppressWarnings("resource")
		PrintStream ps = System.out;new PrintStream(new File("lr-result.txt"));
		for (Instance instance : testSet.allInstance) {
//			ps.println(lr.predict(instance));
		}
	}

	/**
	 * form pair wise training set
	 * @param trainFile
	 * @return training set 
	 * @throws FileNotFoundException
	 */
	public static Instances formTrainSet(String trainFile)
			throws FileNotFoundException {
		HashMap<Integer, Query> queries = new LinkedHashMap<Integer, Query>();
		Scanner scanner = new Scanner(new File(trainFile));
		String line;
		// read query doc pairs and form training set
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();
			String tokens[] = line.split(" ");
			int relevance = Integer.parseInt(tokens[0]);
			String toks[] = tokens[1].split(":");
			int queryId = Integer.parseInt(toks[1]);
			Query query;
			if (queries.containsKey(queryId)) {
				query = queries.get(queryId);
			} else {
				query = new Query(numFeature);
				queries.put(queryId, query);
			}
			Instance instance = new Instance(relevance, numFeature);
			//read features
			for (int i = 2; i < tokens.length; i++) {
				if (tokens[i].startsWith("#")) {
					break;
				}
				String pair[] = tokens[i].split(":");
				instance.addFeature(Integer.parseInt(pair[0]) - 1,
						Double.parseDouble(pair[1]));
			}
			instance.addCustomFeature();
			if (relevance == 0) {
				query.addIrelDoc(instance);
			} else
				query.addRelDoc(instance);
		}
		List<Instance> instances = new ArrayList<Instance>();

		for (int qid : queries.keySet()) {
//			System.out.println(qid);
			Query query = queries.get(qid);
			query.normalize();
			for (Instance instance : query.formTrainSet()) {
				instances.add(instance);
			}
		}
		Instances trainingSet = new Instances(instances, numFeature, 2);
		return trainingSet;
	}

	/**
	 * form test set
	 * @param testFile
	 * @return
	 * @throws FileNotFoundException
	 */
	public static Instances formTestSet(String testFile)
			throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(testFile));
		String line;
		// read query doc pairs and form test set
		List<Instance> instances = new ArrayList<Instance>();
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();
			String tokens[] = line.split(" ");
			int relevance = Integer.parseInt(tokens[0]);
			Instance instance = new Instance(relevance, numFeature);
			for (int i = 2; i < tokens.length; i++) {
				if (tokens[i].startsWith("#")) {
					break;
				}
				String pair[] = tokens[i].split(":");
				instance.addFeature(Integer.parseInt(pair[0]) - 1,
						Double.parseDouble(pair[1]));
			}
			instance.addCustomFeature();
			instances.add(instance);
		}
		Instances testSet = new Instances(instances, numFeature, 2);
		return testSet;
	}
	
	
}
