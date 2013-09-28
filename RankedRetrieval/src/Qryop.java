/*
 *  Copyright (c) 2013, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Qryop {

  protected List<Qryop> args = new ArrayList<Qryop>();
  protected RetrievalAlgorithm algorithm;
  
  /**
   * Evaluates the query operator, including any child operators and returns the result.
   * @return {@link QryResult} object
   * @throws IOException
   */
  public abstract QryResult evaluate() throws IOException;
  

  public void addQryop(Qryop op) {
    args.add(op);
  }
  
  public void setRetrievalAlgorithm(RetrievalAlgorithm algo){
    algorithm = algo;
  }
  
  /**
   * get return type of the operator
   * @return 
   * 	OpType.SCORE if the query operator returns a score list
   * 	OpType.INV if the query operator returns a inverted list
   * 	OpType.WEIGHT if the query operator is an Indri operator
   */
  public abstract OpType getType();
}
