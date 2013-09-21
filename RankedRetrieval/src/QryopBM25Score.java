import java.io.IOException;

public class QryopBM25Score extends Qryop {

    private float k1;
    private float b;
    private float k3;
    private int qtf;

    public QryopBM25Score(Qryop q) {
	this.args.add(q);
    }
    
    public QryopBM25Score(Qryop q, float k1, float b, float k3, int qtf) {
	this.args.add(q);
	this.b = b;
	this.k1 = k1;
	this.k3 = k3;
	this.qtf = qtf;
    }
    
    public QryopBM25Score(Qryop q, Parameter param, int qtf) {
	this.args.add(q);
	this.b = param.bm25_b;
	this.k1 = param.bm25_k1;
	this.k3 = param.bm25_k3;
	this.qtf = qtf;
    }
    
    
    public int getQtf() {
	return qtf;
    }

    public void setQtf(int qtf) {
	this.qtf = qtf;
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
	// TODO determine if it is score list
	QryResult result = args.get(0).evaluate();
	if(result.isScoreList())
	    return result;
	return scoring(result);
		    
    } 
    
    public QryResult scoring(QryResult result) throws IOException {
	String field = result.invertedList.field;
	int N = QryEval.READER.getDocCount(field); //TODO check this getNumDocs or getDocCount
	int df = result.invertedList.df;
	double idf = Math.log((N - df + 0.5) / (df + 0.5));
	double user_weight = 1.0f * (k3 + 1) * qtf / (k3 + qtf);
	float avg_doclen = 1.0f * QryEval.READER.getSumTotalTermFreq(field) / N;
	for (int i = 0; i < df; i++) {
	    DocPosting posting = result.invertedList.postings.get(i);
	    int tf = posting.tf;
	    long doclen = QryEval.dls.getDocLength(field, posting.docid);
	    float score = 1.0f * tf / (tf + k1 * ((1 - b) + b * doclen / avg_doclen));
	    score *= user_weight * idf;
	    result.docScores.add(posting.docid, score);
	}
	return result;
    }

    @Override
    public OpType getType() {
	return OpType.SCORE;
    }
    
}
