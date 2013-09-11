import java.io.IOException;
import java.util.LinkedList;

public class QueryParser {
  private String queryString;
  private RetrievalAlgorithm algorithm;
  public QueryParser(String str, String algo) {
    queryString = str;
    if (algo.equals("UnrankedBoolean")) {
      algorithm = RetrievalAlgorithm.UnrankedBoolean;
    }
    else {
      algorithm = RetrievalAlgorithm.RankedBoolean;
    }
  }

  /**
   * Given a query string, parse it into a implicit tree structure form the query operator
   * 
   * @param queryString
   *          trimmed query string
   * @return query operator
   * @throws IOException 
   */
  public Qryop parse() throws IOException {
    int i = 0;
    Qryop crtOp = null; // operator of the current top level in the query parse tree
    char cur;
    LinkedList<Qryop> qryStack = new LinkedList<Qryop>();
    
    cur = queryString.charAt(i);
    while(cur == ' ' || cur == '\t')
      i++;
    
    if (queryString.charAt(i) != '#') {
      crtOp = new QryopOr(); //TODO modify
      crtOp.setRetrievalAlgorithm(algorithm);
    }

    while (i < queryString.length()) {
      cur = queryString.charAt(i);
      if (cur == ' ' || cur == '\t') {
        i++;
        continue;
      }

      if (cur == '#') { // query operator
        int opStartIndex = i + 1;
        int opEndIndex = queryString.indexOf('(', opStartIndex);
        Qryop op = null;
        String opString = queryString.substring(opStartIndex, opEndIndex).trim();
        if (opString.equals("AND")) {
          op = new QryopAnd();
        } else if (opString.equals("OR")) {
          op = new QryopOr();
        } else if (opString.equals("SYN")) {
          op = new QryopSyn();
        } else if (opString.startsWith("NEAR/")) {
          String pairs[] = opString.split("/");
          int n = Integer.valueOf(pairs[1]);
          op = new QryopNear(n);
        } else {
          System.err.println("Syntax Error!");
          System.exit(1);
        }
        op.setRetrievalAlgorithm(algorithm);
        if (crtOp != null) {
          crtOp.addQryop(op);
          qryStack.push(crtOp);
        }
        crtOp = op;
        i = opEndIndex + 1; // right after '('
      } else if (cur == ')') { // end of query operator
        if(i != queryString.length() - 1)
          crtOp = qryStack.pop();
        i++;
      } else { // query term
        int wordEnd;
        for (wordEnd = i + 1; wordEnd < queryString.length(); wordEnd++) {
          char ch = queryString.charAt(wordEnd);
          if (ch == '\t' || ch == ' ' || ch == ')')
            break;
        }
        if (wordEnd >= queryString.length() && !qryStack.isEmpty()) {
          System.err.println("Stntax Error!");
          System.exit(1);
        }
        String termString = queryString.substring(i, wordEnd);
        String[] strArr = termString.split("\\.");
        Qryop termQuery;
        String processedTerms[] = QryEval.tokenizeQuery(strArr[0]);
        if (processedTerms.length == 0){ //stop words
          i = wordEnd;
          continue;
        }
        String term = processedTerms[0];
        if (strArr.length == 2) // contains field
          termQuery = new QryopTerm(term, strArr[1]);
        else
          termQuery = new QryopTerm(term);
        crtOp.addQryop(termQuery);
        i = wordEnd;
      }
    }
    return crtOp;
  }
  
  public static void main(String args[]) throws Exception {
    String queryStr = "#AND(#OR(aa parsed) cc   #NEAR/3(dd ee))"; 
    QueryParser parser = new QueryParser(queryStr, "UnrankedBoolean");
    Qryop op = parser.parse();
    System.out.println(op);
  }
}
