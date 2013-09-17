import java.io.IOException;

/**
 * near opearator
 * 
 * @author Siping Ji <sipingji@cmu.edu>
 *
 */
public class QryopNear extends Qryop {

  private int neighbor;	

  public QryopNear(int n) {
    neighbor = n;
  }

  @Override
  public QryResult evaluate() throws IOException {

    QryResult result = args.get(0).evaluate();

    // each pass evaluates one query operator
    for (int i = 1; i < args.size(); i++) {
      QryResult iResult = args.get(i).evaluate();

      int rDoc = 0;
      int iDoc = 0;
      QryResult tempResult = new QryResult();
      // walk through inverted lists of both terms to see if there is a match of documents
      while (rDoc < result.invertedList.df && iDoc < iResult.invertedList.df) {
        int rDocId = result.invertedList.postings.get(rDoc).docid;
        int iDocId = iResult.invertedList.postings.get(iDoc).docid;
        if (rDocId < iDocId) {
          rDoc++;
        } else if (rDocId > iDocId) {
          iDoc++;
        } else { // same document matched
          int rPos = 0;
          int iPos = 0;
          DocPosting rPosting = result.invertedList.postings.get(rDoc);
          DocPosting iPosting = iResult.invertedList.postings.get(iDoc);
          DocPosting tmpPosting = new DocPosting(rPosting.docid);
          // walk through position vector of both vectors to see if there are positions 
          // satisfy the neighbor requirement 
          while (rPos < rPosting.tf && iPos < iPosting.tf) {
            if (rPosting.positions.get(rPos) + neighbor < iPosting.positions.get(iPos)) {
              rPos++;
            } else if (rPosting.positions.get(rPos) > iPosting.positions.get(iPos)) {
              iPos++;
            } else { // position matched
              tmpPosting.addPosition(iPosting.positions.get(iPos));
              iPos++;
              rPos++;
            }
          }
          // if theres is a match in both doc and position
          if (tmpPosting.tf != 0) {
            tempResult.invertedList.addPosting(tmpPosting);
          }
          iDoc++;
          rDoc++;
        }
      }
      result = tempResult;
    }
    QryopScore impliedOp = new QryopScore(this.algorithm);
    return impliedOp.scoring(result);
  }

  public String toString() {
    StringBuffer buf = new StringBuffer("#Near/");
    buf.append(Integer.toString(neighbor));
    buf.append("(");
    for (int i = 0; i < args.size(); i++) {
      buf.append(args.get(i).toString());
      buf.append(i == args.size() - 1 ? ")" : " ");
    }
    return buf.toString();

  }

}
