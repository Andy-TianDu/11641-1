import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class QryopWindow extends Qryop {
    private int window;

    public QryopWindow(int w) {
	window = w;
    }

    @Override
    public QryResult evaluate() throws IOException {
	ArrayList<QryResult> resultList = new ArrayList<QryResult>();
	for (int i = 0; i < args.size(); i++) {
	    resultList.add(args.get(i).evaluate());
	}

	int docIndices[] = new int[args.size()];
	int posIndices[] = new int[args.size()];
	Arrays.fill(docIndices, 0);
	boolean docStopFlag = false;
	QryResult result = new QryResult();
	result.invertedList.field = resultList.get(0).invertedList.field;
	while (true) {
	    boolean sameDocFlag = true;
	    for (int i = 0; i < args.size(); i++) {
		if (docIndices[i] >= resultList.get(i).invertedList.df) {
		    docStopFlag = true;
		}
	    }
	    if (docStopFlag)
		break;
	    int docId = resultList.get(0).invertedList.postings
		    .get(docIndices[0]).docid;
	    int maxDocId = docId;
	    for (int i = 1; i < args.size(); i++) {
		int iDocId = resultList.get(i).invertedList.postings
			.get(docIndices[i]).docid;
		if (iDocId != docId) {
		    sameDocFlag = false;
		}
		maxDocId = Math.max(iDocId, maxDocId);
	    }

	    if (sameDocFlag) {
		// check positions then advance all
		Arrays.fill(posIndices, 0);
		boolean posStopFlag = false;
		DocPosting postings[] = new DocPosting[args.size()];
		for (int i = 0; i < args.size(); i++) {
		    postings[i] = resultList.get(i).invertedList.postings
			    .get(docIndices[i]);
		}
		DocPosting tmpPosting = new DocPosting(docId);
		while (true) {
		    int positions[] = new int[args.size()];
		    int minPos, maxPos;
		    minPos = Integer.MAX_VALUE;
		    maxPos = Integer.MIN_VALUE;
		    for (int i = 0; i < args.size(); i++) {
			if (posIndices[i] >= postings[i].tf) {
			    posStopFlag = true;
			    break;
			}
			else {
			    positions[i] = postings[i].positions.get(posIndices[i]);
			    minPos = Math.min(positions[i], minPos);
			    maxPos = Math.max(positions[i], maxPos);
			}
		    }
		    
		    if (posStopFlag) 
			break;
		    
		    // if max pos - min pos <= window_size, add to result
		    // else increase the indices with min pos
		    if (maxPos - minPos <= window) {
			tmpPosting.addPosition(maxPos);
			for (int i = 0; i < args.size(); i++)
			    posIndices[i] ++;
		    }
		    else {
			for (int i = 0; i < args.size(); i++) {
			    if(positions[i] == minPos) {
				posIndices[i] ++;
			    }
			}
		    }
		    
		}
		
		if (tmpPosting.tf != 0) {
		    result.invertedList.addPosting(tmpPosting);
		}

		for (int i = 0; i < args.size(); i++) {
		    docIndices[i]++;
		}
	    } else {
		// advance only indices with small docids
		for (int i = 0; i < args.size(); i++) {
		    if (resultList.get(i).invertedList.postings.get(docIndices[i]).docid != maxDocId) {
			docIndices[i]++;
		    }
		}
	    }
	}
	
	return result;
	// QryResult result = args.get(0).evaluate();
	// String field = result.invertedList.field;
	// // each pass evaluates one query operator
	// for (int i = 1; i < args.size(); i++) {
	// QryResult iResult = args.get(i).evaluate();
	//
	// int rDoc = 0;
	// int iDoc = 0;
	// QryResult tempResult = new QryResult();
	// // walk through inverted lists of both terms to see if there is a
	// // match of documents
	// while (rDoc < result.invertedList.df
	// && iDoc < iResult.invertedList.df) {
	// int rDocId = result.invertedList.postings.get(rDoc).docid;
	// int iDocId = iResult.invertedList.postings.get(iDoc).docid;
	// if (rDocId < iDocId) {
	// rDoc++;
	// } else if (rDocId > iDocId) {
	// iDoc++;
	// } else { // same document matched
	// int rPos = 0;
	// int iPos = 0;
	// DocPosting rPosting = result.invertedList.postings
	// .get(rDoc);
	// DocPosting iPosting = iResult.invertedList.postings
	// .get(iDoc);
	// DocPosting tmpPosting = new DocPosting(rPosting.docid);
	// // walk through position vector of both vectors to see if
	// // there are positions
	// // satisfy the neighbor requirement
	// while (rPos < rPosting.tf && iPos < iPosting.tf) {
	// if (rPosting.positions.get(rPos) + window < iPosting.positions
	// .get(iPos)) {
	// rPos++;
	// } else if (rPosting.positions.get(rPos) - window > iPosting.positions
	// .get(iPos)) {
	// iPos++;
	// } else { // position matched
	// tmpPosting
	// .addPosition(iPosting.positions.get(iPos));
	// iPos++;
	// rPos++;
	// }
	// }
	// // if theres is a match in both doc and position
	// if (tmpPosting.tf != 0) {
	// tempResult.invertedList.addPosting(tmpPosting);
	// }
	// iDoc++;
	// rDoc++;
	// }
	// }
	// result = tempResult;
	// }
	// result.invertedList.field = field;
	// return result;
    }

    public String toString() {
	StringBuffer buf = new StringBuffer("#Window/");
	buf.append(Integer.toString(window));
	buf.append("(");
	for (int i = 0; i < args.size(); i++) {
	    buf.append(args.get(i).toString());
	    buf.append(i == args.size() - 1 ? ")" : " ");
	}
	return buf.toString();

    }

    @Override
    public int hashCode() {
	return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
	return this.toString().equals(obj.toString());
    }

    @Override
    public OpType getType() {
	return OpType.INV;
    }

}
