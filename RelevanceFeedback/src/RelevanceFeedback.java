import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * RelevanceFeedback This class transforms a query to an expanded query using
 * Pseudo Relevance Feedback
 * 
 * @author Siping Ji <sipingji@cmu.edu>
 * 
 */
public class RelevanceFeedback {

    private int numDocs;
    private int numTerms;
    private int mu;
    private float originalWeight;
    private String field = "body";
    private Parameter param;
    private Qryop originalQuery;

    public RelevanceFeedback(int qid, Qryop query, Parameter param) {
	this.param = param;
	originalQuery = query;
	numDocs = param.fb_numDocs;
	numTerms = param.fb_numTerms;
	mu = param.fb_mu;
	originalWeight = param.fb_originalWeight;
    }

    /**
     * evaluate - expand query using pseudo relevance feedback
     * the steps are as follows:
     * 1.evaluate the original query
     * 2.estimate term probability using top docs returned
     * 3.expand original query
     * 
     * @return expanded query
     * @throws IOException
     */
    public Qryop evaluate() throws IOException {

	// store the term - probability pairs
	HashMap<String, Float> vocabulary = new HashMap<String, Float>();
	// first evaluate the original query and fetch the score list
	QryResult firstResult = originalQuery.evaluate();
	firstResult.docScores.sort();

	int N = QryEval.READER.numDocs();
	float avg_doclen = 1.0f * QryEval.READER.getSumTotalTermFreq(field) / N;
	long length_c = QryEval.READER.getSumTotalTermFreq(field);
	long length_d[] = new long[numDocs];
	double p_d[] = new double[numDocs];
	TermVector tv[] = new TermVector[numDocs];
	// for each doc in top numDocs
	// initialize document statistics
	for (int i = 0; i < numDocs && i < firstResult.docScores.scores.size(); i++) {
	    int docId = firstResult.docScores.getDocid(i);
	    length_d[i] = QryEval.dls.getDocLength(field, docId);
	    p_d[i] = Math.exp(firstResult.docScores.getDocidScore(i));
	    tv[i] = new TermVector(docId, field);
	}

	// for each doc in top numDocs
	for (int i = 0; i < numDocs && i < firstResult.docScores.scores.size(); i++) {
	    // for each term appeared
	    // compute a default score
	    for (int j = 1; j < tv[i].stemsLength(); j++) {
		String stem = tv[i].stemString(j);
		if (!vocabulary.containsKey(stem)) {
		    double score = 0;
		    long ctf = tv[i].totalStemFreq(j);
		    double pMLE = 1.0d * ctf / length_c;
		    for (int k = 0; k < numDocs
			    && k < firstResult.docScores.scores.size(); k++) {
			score += mu * pMLE / (avg_doclen + mu) * p_d[k];
			vocabulary.put(stem, (float) score);
		    }
		}
	    }
	}

	for (int i = 0; i < numDocs && i < firstResult.docScores.scores.size(); i++) {
	    // for each term
	    // update estimated probability if term j appeared in document i
	    for (int j = 1; j < tv[i].stemsLength(); j++) {
		String stem = tv[i].stemString(j);
		double score = vocabulary.get(stem);
		long ctf = tv[i].totalStemFreq(j);
		double pMLE = 1.0d * ctf / length_c;
		score -= mu * pMLE / (avg_doclen + mu) * p_d[i];
		score += 1.0d * (tv[i].stemFreq(j) + mu * pMLE)
			/ (length_d[i] + mu) * p_d[i];
		vocabulary.put(stem, (float) score);
	    }
	}

	// sort the terms by estimated probability
	List<Entry<String, Float>> sortedTerm = sortMap(vocabulary);
	QryopIndriWeight qe = new QryopIndriWeight(param);

	// form the expanded query
	for (int i = 0; i < numTerms; i++) {
	    Entry<String, Float> entry = sortedTerm.get(i);
	    qe.addQryop(new QryopTerm(entry.getKey()));
	    qe.addWeight(entry.getValue());
	}
	QryopIndriWeight qExpand = new QryopIndriWeight(param);
	qExpand.addQryop(originalQuery);
	qExpand.addWeight(originalWeight);
	qExpand.addQryop(qe);
	qExpand.addWeight(1 - originalWeight);

	return qExpand;
    }

    /**
     * sortMap - sort the original vocabulary according to estimated the
     * probability
     * 
     * @param map
     *            vocabulary map
     * @return a sorted list containing entries of term and probability pair
     */
    public static List<Entry<String, Float>> sortMap(Map<String, Float> map) {
	List<Entry<String, Float>> sortedList = new LinkedList<Entry<String, Float>>(
		map.entrySet());
	Collections.sort(sortedList, new Comparator<Entry<String, Float>>() {

	    @Override
	    public int compare(Entry<String, Float> o1, Entry<String, Float> o2) {
		return o2.getValue().compareTo(o1.getValue());
	    }
	});
	return sortedList;
    }
}
