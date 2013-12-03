import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Instances {
	List<Instance> allInstance;

	private int numFeature = 0;
	private int numClass = 0;
	private HashMap<Integer, Integer> labelMap;

	public Instances() {
		allInstance = new ArrayList<Instance>();
	}
	
	public Instances(List<Instance> instances, int numFeature, int numClass) {
		allInstance = instances;
		this.numClass = numClass;
		this.numFeature = numFeature;
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
	
	public void normalize() {
		for (Instance instance : allInstance) {
			instance.normalize();
		}
	}

}
