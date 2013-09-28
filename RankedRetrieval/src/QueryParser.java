import java.io.IOException;
import java.util.LinkedList;

/**
 * query parse that parse the prefix query string
 * 
 * @author Siping Ji <siping@cmu.edu>
 * 
 */
public class QueryParser {
    private String queryString;
    private RetrievalAlgorithm algorithm; // ranked boolean or unraked boolean
    private Parameter param;

    public QueryParser(String str, String algo, Parameter param) {
	queryString = str;
	if (algo.equals("UnrankedBoolean")) {
	    algorithm = RetrievalAlgorithm.UnrankedBoolean;
	} else if (algo.equals("RankedBoolean")) {
	    algorithm = RetrievalAlgorithm.RankedBoolean;
	} else if (algo.equals("BM25")) {
	    algorithm = RetrievalAlgorithm.BM25;
	} else {
	    algorithm = RetrievalAlgorithm.Indri;
	}
	this.param = param;
    }

    /**
     * Given a query string, parse it into a implicit tree structure form the
     * query operator
     * 
     * @param queryString
     *            trimmed query string
     * @return query operator
     * @throws IOException
     */
    public Qryop parse() throws IOException {
	int i = 0;
	Qryop crtOp = null; // operator of the current top level in the query
			    // parse tree
	char cur;
	LinkedList<Qryop> qryStack = new LinkedList<Qryop>();

	cur = queryString.charAt(i);
	while (cur == ' ' || cur == '\t')
	    i++;

	if (queryString.charAt(i) != '#') {
	    switch (algorithm) {
	    case BM25:
		crtOp = new QryopBM25SUM(param);
		break;
	    case Indri:
		crtOp = new QryopIndriAnd(param);
		break;
	    default:
		crtOp = new QryopOr();
	    }
	    crtOp.setRetrievalAlgorithm(algorithm);
	}

	while (i < queryString.length()) {
	    cur = queryString.charAt(i);
	    if (cur == ' ' || cur == '\t' || cur == '\r' || cur == '\n') {
		i++;
		continue;
	    }

	    if (cur == '#') { // query operator
		int opStartIndex = i + 1;
		int opEndIndex = queryString.indexOf('(', opStartIndex);
		Qryop op = null;
		String opString = queryString.substring(opStartIndex,
			opEndIndex).trim();
		if (opString.equalsIgnoreCase("AND")) {
		    op = new QryopAnd();
		} else if (opString.equalsIgnoreCase("OR")) {
		    op = new QryopOr();
		} else if (opString.equalsIgnoreCase("SYN")) {
		    op = new QryopSyn();
		} else if (opString.startsWith("NEAR/")) {
		    String pairs[] = opString.split("/");
		    int n = Integer.valueOf(pairs[1].trim());
		    op = new QryopNear(n);
		} else if (opString.startsWith("UW/")) {
		    String pairs[] = opString.split("/");
		    int w = Integer.valueOf(pairs[1].trim());
		    op = new QryopWindow(w);
		} else if (opString.equalsIgnoreCase("SUM")) {
		    op = new QryopBM25SUM(param);
		} else if (opString.equalsIgnoreCase("COMBINE")) {
		    op = new QryopIndriAnd(param);
		} else if (opString.equalsIgnoreCase("WEIGHT")) {
		    op = new QryopIndriWeight(param);
		} else {
		    System.err.println("Syntax Error!");
		    System.exit(1);
		}
		op.setRetrievalAlgorithm(algorithm);
		if (crtOp != null) {
		    crtOp.addQryop(op);
		    qryStack.push(crtOp);
		} else { // first operator
		    if (op.getType() == OpType.INV) {
			switch (algorithm) {
			case BM25:
			    crtOp = new QryopBM25SUM(param);
			    break;
			case Indri:
			    crtOp = new QryopIndriAnd(param);
			    break;
			default:
			    crtOp = new QryopOr();
			}
			crtOp.setRetrievalAlgorithm(algorithm);
			crtOp.addQryop(op);
			qryStack.push(crtOp);
		    }
		}
		crtOp = op;
		i = opEndIndex + 1; // right after '('
	    } else if (cur == ')') { // end of query operator
		if (i != queryString.length() - 1)
		    crtOp = qryStack.pop();
		else { // NEAR, WINDOW in the outer layer
		    if (crtOp.getType() == OpType.INV && qryStack.isEmpty()) {
			Qryop op = null;
			switch (algorithm) {
			case BM25:
			    op = new QryopBM25SUM(param);
			    break;
			case Indri:
			    op = new QryopIndriAnd(param);
			    break;
			default:
			    op = new QryopOr();
			}
			op.setRetrievalAlgorithm(algorithm);
			op.addQryop(crtOp);
			crtOp = op;
		    }
		}
		i++;
	    } else { // query term or weight
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

		if (cur >= '0' && cur <= '9'
			&& crtOp.getType() == OpType.WEIGHT) {// might be weight
		    QryopIndriWeight weightOp = (QryopIndriWeight) crtOp;
		    if (weightOp.getNumOperands() == weightOp.getNumWeights()) {
			//the number is a weight but not a term
			String weightString = queryString.substring(i, wordEnd);
			float weight = Float.valueOf(weightString);
			weightOp.addWeight(weight);
			i = wordEnd;
			continue;
		    }
		}

		String termString = queryString.substring(i, wordEnd);
		String[] strArr = termString.split("\\.");
		Qryop termQuery;
		String processedTerms[] = QryEval.tokenizeQuery(strArr[0]);
		if (processedTerms.length == 0) { // stop words
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
	if (!qryStack.isEmpty()) {
	    crtOp = qryStack.pop();
	}
	System.out.println(crtOp);
	return crtOp;
    }

    public static void main(String args[]) throws Exception {
	String queryStr = "#COMBINE( #NEAR/1( charleston sc )  #NEAR/1( yorktown charleston )  #NEAR/1( uss yorktown ) )";
	QueryParser parser = new QueryParser(queryStr, "Indri", new Parameter());
	Qryop op = parser.parse();
    }
}
