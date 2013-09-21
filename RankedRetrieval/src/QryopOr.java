import java.io.IOException;

/**
 * Query Or 
 * 
 * @author Siping Ji <sipingji@cmu.edu>
 *
 */
public class QryopOr extends Qryop {

  @Override
  public QryResult evaluate() throws IOException {
    Qryop impliedQryOp = new QryopScore(args.get(0), algorithm);
    QryResult result = impliedQryOp.evaluate();

    // Each pass of the loop evaluates one query argument.
    for (int i = 1; i < args.size(); i++) {

      impliedQryOp = new QryopScore(args.get(i), algorithm);
      QryResult iResult = impliedQryOp.evaluate();

      // Use the results of the i'th argument to incrementally compute the query operator.
      // Intersection-style query operators iterate over the incremental results, not the results of
      // the i'th query argument.
      int rDoc = 0; /* Index of a document in result. */
      int iDoc = 0; /* Index of a document in iResult. */
      QryResult tempResult = new QryResult();
      
      // walk through score lists
      // add the doc to the new scored list if it's in either score list
      while (rDoc < result.docScores.scores.size() && iDoc < iResult.docScores.scores.size()) {
        if (result.docScores.getDocid(rDoc) == iResult.docScores.getDocid(iDoc)) {
          if (algorithm == RetrievalAlgorithm.RankedBoolean) // ranked boolean, compute Max score
            tempResult.docScores.add(
                    result.docScores.getDocid(rDoc),
                    Math.max(result.docScores.getDocidScore(rDoc),
                            iResult.docScores.getDocidScore(iDoc)));
          else
            tempResult.docScores.add(result.docScores.getDocid(rDoc), 1.0f);
          iDoc++;
          rDoc++;
        } else if (result.docScores.getDocid(rDoc) < iResult.docScores.getDocid(iDoc)) {
          tempResult.docScores.add(result.docScores.getDocid(rDoc),
                  result.docScores.getDocidScore(rDoc));
          rDoc++;
        } else {
          tempResult.docScores.add(iResult.docScores.getDocid(iDoc),
                  iResult.docScores.getDocidScore(iDoc));
          iDoc++;
        }
      }
      // walk through the rest of the score list
      while (rDoc < result.docScores.scores.size()) {
        tempResult.docScores.add(result.docScores.getDocid(rDoc),
                result.docScores.getDocidScore(rDoc));
        rDoc++;
      }
      while (iDoc < iResult.docScores.scores.size()) {
        tempResult.docScores.add(iResult.docScores.getDocid(iDoc),
                iResult.docScores.getDocidScore(iDoc));
        iDoc++;
      }
      result = tempResult;
    }

    return result;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer("#Or(");
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
