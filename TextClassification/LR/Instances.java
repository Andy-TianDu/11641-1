import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Instances {
	List<Instance> allInstance = new ArrayList<Instance>();

	private int numFeature = 0;
	private int numClass = 0;
	private HashMap<Integer, Integer> labelMap;

	public Instances() {
	}

	public Instances(String fileName) {
		readFromFile(fileName);
	}

	public void readFromFile(String fileName) {
		labelMap = new HashMap<Integer, Integer>();
		Scanner scanner;
		try {
			scanner = new Scanner(new File(fileName));
			String line;
			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				String[] tokens = line.trim().split(" ");
				int label = Integer.parseInt(tokens[0]);
				if (labelMap.containsKey(label)) {
					labelMap.put(label, labelMap.get(label) + 1);
				}
				else {
					labelMap.put(label, 1);
				}
				Instance instance = new Instance(label);
				for (int i = 1; i < tokens.length; i++) {
					String[] toks = tokens[i].split(":");
					int featId = Integer.parseInt(toks[0]);
					float value = Float.parseFloat(toks[1]);
					instance.addFeature(featId, value);
					numFeature = Math.max(numFeature, featId);
				}
				instance.feature.put(0, 1.0d); // intercept term
				allInstance.add(instance);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		numFeature += 1;
		numClass = labelMap.size();
	}

	public int numInstance() {
		return allInstance.size();
	}

	public int numFeature() {
		return numFeature;
	}

	public int numClass() {
		return numClass;
	}

	public Instance getInstance(int i) {
		return allInstance.get(i);
	}

	public void shuffle() {
		Collections.shuffle(allInstance);
	}
	
	public int numPos(int label) {
		return labelMap.get(label);
	}

	public static void main(String args[]) throws FileNotFoundException {
		Instances instances = new Instances("data/citeseer.train.ltc.svm");
		System.out.println(instances.allInstance.get(0));
	}
}
