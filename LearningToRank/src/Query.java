import java.util.ArrayList;
import java.util.List;
/**
 * query class that store query doc pairs
 * @author Siping Ji <sipingji@cmu.edu>
 *
 */
public class Query {
	public List<Instance> relDocs = new ArrayList<Instance>();
	public List<Instance> irelDocs = new ArrayList<Instance>();
	public List<Instance> partialRelSet = new ArrayList<Instance>();

	/**
	 * form training set from partial relevance judgment
	 * @return
	 */
	public List<Instance> formTrainSet() {

		Instance instance;
		for (Instance rel : relDocs) {
			for (Instance irel : irelDocs) {
				instance = new Instance(1);
				for (int fid : rel.feature.keySet()) {
					instance.addFeature(fid, rel.feature.get(fid)
							- irel.feature.get(fid));
				}
				partialRelSet.add(instance);
			}
		}
		return partialRelSet;
	}
	
	public void addRelDoc(Instance instance) {
		relDocs.add(instance);
	}
	
	public void addIrelDoc(Instance instance) {
		irelDocs.add(instance);
	}
	
	public void normalize() {
		for (Instance instance : relDocs) {
			instance.normalize();
		}
		for (Instance instance : irelDocs) {
			instance.normalize();
		}
	}
}
