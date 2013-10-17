import java.io.IOException;

/**
 * This class scores a result with inverted list according to Indri model
 * 
 * @author Siping Ji <sipingji@cmu.edu>
 * 
 */
public class QryopIndriScore extends Qryop {

    private int mu;
    private float lambda;
    private String smoothing;

    public QryopIndriScore(Qryop arg, Parameter param) {
	this.args.add(arg);
	mu = param.indri_mu;
	lambda = param.indri_lambda;
	smoothing = param.indri_smoothing;
    }

    @Override
    public QryResult evaluate() throws IOException {
	// TODO determine if it is score list
	QryResult result = args.get(0).evaluate();
	if (result.isScoreList())
	    return result;
	return scoring(result);

    }

    /**
     * compute score according to Indri model using two-stage smoothing
     * 
     * @param result
     * @return
     * @throws IOException
     */
    public QryResult scoring(QryResult result) throws IOException {

	String field = result.invertedList.field;
	long ctf = result.invertedList.ctf;
	long length_c = QryEval.READER.getSumTotalTermFreq(field);

	int j = 0;
	int df = result.invertedList.df;
	int N = QryEval.READER.numDocs();
	float avg_doclen = 1.0f * QryEval.READER.getSumTotalTermFreq(field) / N;
	
	float smoothingTerm;
	if (smoothing.equals("df")) {
	    smoothingTerm = 1.0f * df / N;
	} else {
	    smoothingTerm = 1.0f * ctf / length_c;
	}
	float default_score = lambda * (mu * smoothingTerm) / (avg_doclen + mu)
		    + (1 - lambda) * smoothingTerm;
	if (default_score == 0)
	    default_score = Float.MIN_VALUE;
	default_score = (float) Math.log(default_score);
	for (int i = 0; i < QryEval.READER.numDocs(); i++) {
	    if (j < df) {
		DocPosting posting = result.invertedList.postings.get(j);
		if (i == posting.docid) {
		    long length_d = QryEval.dls.getDocLength(field,
			    posting.docid);
		    long tf = posting.tf;
		    float score = lambda * (tf + mu * smoothingTerm)
			    / (length_d + mu) + (1 - lambda) * smoothingTerm;
		    score = (float) Math.log(score);
		    result.docScores.add(posting.docid, score);
		    j++;
		    continue;
		}
	    }

//	    long length_d = QryEval.dls.getDocLength(field, i);

//	    float score = lambda * (mu * smoothingTerm) / (avg_doclen + mu)
//		    + (1 - lambda) * smoothingTerm;
//	    // when ctf, tf == 0(NEAR), smoothing term = 0, score = 0,
	    // log(score) = -inf
	    // this is problemetic, so in this situation, I choose to assign
	    // score = min float value
//	    if (default_score == 0)
//		default_score = Float.MIN_VALUE;
//	    score = (float) Math.log(score);
	    result.docScores.add(i, default_score);
	}
	return result;
    }

    @Override
    public OpType getType() {
	return OpType.SCORE;
    }

}
