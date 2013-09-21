import java.io.IOException;
import java.util.HashMap;

public class QryopBM25SUM extends Qryop {
    private float k1;
    private float b;
    private float k3;

    public QryopBM25SUM(Parameter param) {
	k1 = param.bm25_k1;
	b = param.bm25_b;
	k3 = param.bm25_k3;
    }

    public float getK1() {
	return k1;
    }

    public void setK1(float k1) {
	this.k1 = k1;
    }

    public float getB() {
	return b;
    }

    public void setB(float b) {
	this.b = b;
    }

    public float getK3() {
	return k3;
    }

    public void setK3(float k3) {
	this.k3 = k3;
    }

    @Override
    public QryResult evaluate() throws IOException {
	// TODO Auto-generated method stub
	HashMap<Qryop, Integer> terms = new HashMap<Qryop, Integer>();

	for (Qryop arg : args) {
	    if (!terms.containsKey(arg))
		terms.put(arg, 1);
	    else
		terms.put(arg, terms.get(arg) + 1);
	}
	QryResult iResult = new QryResult();
	for (Qryop term : terms.keySet()) {
	    QryopBM25Score impliedOp = new QryopBM25Score(term, k1, b, k3,
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

}
