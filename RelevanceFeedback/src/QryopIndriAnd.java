import java.io.IOException;

/**
 * This class defines the behavior of the indri combine operator
 * 
 * @author Siping Ji <sipingji@cmu.edu>
 * 
 */
public class QryopIndriAnd extends Qryop {

    private Parameter param;

    public QryopIndriAnd(Parameter param) {
	this.param = param;
    }

    @Override
    /*
     * (non-Javadoc)
     * 
     * @see Qryop#evaluate()
     */
    public QryResult evaluate() throws IOException {
	float weight = 1.0f / args.size(); // even weight

	QryopIndriScore impliedOp = new QryopIndriScore(args.get(0), param);
	QryResult result = impliedOp.evaluate();

	for (int i = 0; i < QryEval.READER.numDocs(); i++) {
	    float score = result.docScores.getDocidScore(i);
	    result.docScores.setScore(i, score * weight);
	}
	// Each pass of the loop evaluates one query argument.
	for (int i = 1; i < args.size(); i++) {
	    impliedOp = new QryopIndriScore(args.get(i), param);
	    QryResult iResult = impliedOp.evaluate();
	    // simply aggregate the scores of for each score list
	    // the average and compute
	    for (int j = 0; j < QryEval.READER.numDocs(); j++) {
		float rScore = result.docScores.getDocidScore(j);
		float iScore = iResult.docScores.getDocidScore(j);
		result.docScores.setScore(j, rScore + iScore * weight);
	    }
	}
	return result;
    }

    public String toString() {
	StringBuffer buf = new StringBuffer("#And(");
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
