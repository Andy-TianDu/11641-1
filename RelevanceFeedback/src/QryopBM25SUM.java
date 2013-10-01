import java.io.IOException;
import java.util.HashMap;

/**
 * This class defines the behavior of BM25 SUM operator
 * 
 * @author Siping Ji <sipingji@cmu.edu>
 * 
 */
public class QryopBM25SUM extends Qryop {
    private Parameter param;

    public QryopBM25SUM(Parameter param) {
	this.param = param;
    }

    @Override
    public QryResult evaluate() throws IOException {
	// calculate qtf
	// duplicate query term will be evaluate only once with qtf > 1
	// in this implementation, near/window operator will also be
	// considered to be potentially duplicated if two ops contain the same
	// terms, for example NEAR/1(a b) and NEAR/1(a b) will only be evaluated
	// once with qtf = 2
	// this is implemented by override the hashcode and equals method of
	// QryOpWindow and QryopNear
	HashMap<Qryop, Integer> terms = new HashMap<Qryop, Integer>();

	for (Qryop arg : args) {
	    if (!terms.containsKey(arg))
		terms.put(arg, 1);
	    else
		terms.put(arg, terms.get(arg) + 1);
	}
	QryResult iResult = new QryResult();
	
	// for each term or sub-operator, compute their score
	// and aggregate them to get the whole score
	for (Qryop term : terms.keySet()) {
	    QryopBM25Score impliedOp = new QryopBM25Score(term, param,
		    terms.get(term));
	    QryResult result = impliedOp.evaluate();
	    int iDoc, rDoc;
	    iDoc = rDoc = 0;
	    QryResult tmpResult = new QryResult();
	    while (iDoc < iResult.docScores.scores.size()
		    && rDoc < result.docScores.scores.size()) {
		int iDocId = iResult.docScores.getDocid(iDoc);
		int rDocId = result.docScores.getDocid(rDoc);
		float iScore = iResult.docScores.getDocidScore(iDoc);
		float rScore = result.docScores.getDocidScore(rDoc);
		if (iDocId == rDocId) {
		    tmpResult.docScores.add(iDocId, iScore + rScore);
		    iDoc++;
		    rDoc++;
		} else if (iDocId < rDocId) {
		    tmpResult.docScores.add(iDocId, iScore);
		    iDoc++;
		} else {
		    tmpResult.docScores.add(rDocId, rScore);
		    rDoc++;
		}
	    }
	    while (iDoc < iResult.docScores.scores.size()) {
		int iDocId = iResult.docScores.getDocid(iDoc);
		float iScore = iResult.docScores.getDocidScore(iDoc);
		tmpResult.docScores.add(iDocId, iScore);
		iDoc++;
	    }
	    while (rDoc < result.docScores.scores.size()) {
		int rDocId = result.docScores.getDocid(rDoc);
		float rScore = result.docScores.getDocidScore(rDoc);
		tmpResult.docScores.add(rDocId, rScore);
		rDoc++;
	    }
	    iResult = tmpResult;
	}
	return iResult;
    }

    @Override
    public String toString() {
	StringBuffer buf = new StringBuffer("#SUM(");
	for (int i = 0; i < args.size(); i++) {
	    buf.append(args.get(i).toString());
	    buf.append(i == args.size() - 1 ? ")" : " ");
	}
	return buf.toString();
    }

    @Override
    public OpType getType() {
	return OpType.SCORE;
    }

}
