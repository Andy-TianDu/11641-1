/*
 *  Copyright (c) 2013, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.IOException;

public class QryopScore extends Qryop {

  /**
   * The SCORE operator accepts just one argument.
   */
  public QryopScore(Qryop q, RetrievalAlgorithm algo) {
    this.args.add(q);
    algorithm = algo;
  }

  public QryopScore(RetrievalAlgorithm algo) {
    algorithm = algo;
  }

  /**
   * Evaluate the query operator.
   */
  public QryResult evaluate() throws IOException {

    // Evaluate the query argument.
    QryResult result = args.get(0).evaluate();
    if (result.isScoreList()) //test if the result already has a scores list
      return result;

    return scoring(result);
  }
  
  /**
   * add score list to the result
   * this method is only called when param result does not contains a scores list
   * @param result
   * @return
   */
  public QryResult scoring(QryResult result) {
    // Each pass of the loop computes a score for one document. Note: If the evaluate operation
    // above returned a score list (which is very possible), this loop gets skipped.
    for (int i = 0; i < result.invertedList.df; i++) {

      // DIFFERENT RETRIEVAL MODELS IMPLEMENT THIS DIFFERENTLY.
      // Unranked Boolean. All matching documents get a score of 1.0.
      if (algorithm == RetrievalAlgorithm.UnrankedBoolean) {
        result.docScores.add(result.invertedList.postings.get(i).docid, (float) 1.0);
      }
      // Ranked Boolean. Assign term frequency as the score
      else
        result.docScores.add(result.invertedList.postings.get(i).docid,
                (float) result.invertedList.postings.get(i).tf);
    }

    return result;
  }
  
  @Override
  public OpType getType() {
	return OpType.SCORE;
  }

}
