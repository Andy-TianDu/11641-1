import java.io.IOException;
import java.util.ArrayList;

public class QryopIndriWeight extends Qryop {

    private Parameter param;
    private ArrayList<Float> weights = new ArrayList<Float>();
    private float totalWeight = 0;
    public QryopIndriWeight(Parameter param) {
	this.param = param;
    }

    public int getNumOperands() {
	return args.size();
    }

    public int getNumWeights() {
	return weights.size();
    }

    public void addWeight(float weight) {
	weights.add(weight);
	totalWeight += weight;
    }
    
    @Override
    public QryResult evaluate() throws IOException {	
	QryopIndriScore impliedOp = new QryopIndriScore(args.get(0), param);
	QryResult result = impliedOp.evaluate();
	for (int i = 0; i < QryEval.READER.numDocs(); i++) {
	    float score = result.docScores.getDocidScore(i);
	    result.docScores.setScore(i, score * weights.get(0) / totalWeight);
	}
	// Each pass of the loop evaluates one query argument.
	for (int i = 1; i < args.size(); i++) {
	    impliedOp = new QryopIndriScore(args.get(i), param);
	    QryResult iResult = impliedOp.evaluate();
	    for (int j = 0; j < QryEval.READER.numDocs(); j++) {
		float rScore = result.docScores.getDocidScore(j);
		float iScore = iResult.docScores.getDocidScore(j);
		result.docScores.setScore(j, rScore + iScore * weights.get(i) / totalWeight);
	    }
	}
	return result;
    }

    public String toString() {
	StringBuffer buf = new StringBuffer("#Weight(");
	for (int i = 0; i < args.size(); i++) {
	    buf.append(weights.get(i));
	    buf.append(" ");
	    buf.append(args.get(i).toString());
	    buf.append(i == args.size() - 1 ? ")" : " ");
	}
	return buf.toString();
    }

    @Override
    public OpType getType() {
	// TODO Auto-generated method stub
	return OpType.WEIGHT;
    }

}
