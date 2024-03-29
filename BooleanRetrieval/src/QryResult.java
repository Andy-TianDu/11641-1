/*
 *  Copyright (c) 2013, Carnegie Mellon University.  All Rights Reserved.
 */

public class QryResult {

  // Different types of query operators produce different types of results.
  ScoreList docScores = new ScoreList();

  InvList invertedList = new InvList();

  public boolean isScoreList() {
    return docScores.getScoreListLength() > 0 || invertedList.postings.size() == 0;
  }
}
