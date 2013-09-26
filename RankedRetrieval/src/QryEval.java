/*
 *  This software illustrates the architecture for the portion of a
 *  search engine that evaluates queries.  It is a template for class
 *  homework assignments, so it emphasizes simplicity over efficiency.
 *  It implements an unranked Boolean retrieval model, however it is
 *  easily extended to other retrieval models.  For more information,
 *  see the ReadMe.txt file.
 *
 *  Copyright (c) 2013, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class QryEval {

    static String usage = "Usage:  java "
	    + System.getProperty("sun.java.command") + " paramFile\n\n";

    static String oFile = "result.txt";

    /**
     * The index file reader is accessible via a global variable. This isn't
     * great programming style, but the alternative is for every query operator
     * to store or pass this value, which creates its own headaches.
     */
    public static IndexReader READER;
    public static DocLengthStore dls;

    public static EnglishAnalyzerConfigurable analyzer = new EnglishAnalyzerConfigurable(
	    Version.LUCENE_43);
    static {
	analyzer.setLowercase(true);
	analyzer.setStopwordRemoval(true);
	analyzer.setStemmer(EnglishAnalyzerConfigurable.StemmerType.KSTEM);
    }

    /**
     * 
     * @param args
     *            The only argument should be one file name, which indicates the
     *            parameter file.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
//	long startTime = System.currentTimeMillis();
	// must supply parameter file
	if (args.length < 1) {
	    System.err.println(usage);
	    System.exit(1);
	}

	// read in the parameter file; one parameter per line in format of
	// key=value
	Map<String, String> params = new HashMap<String, String>();
	Scanner scan = new Scanner(new File(args[0]));
	String line = null;
	do {
	    line = scan.nextLine();
	    String[] pair = line.split("=");
	    params.put(pair[0].trim(), pair[1].trim());
	} while (scan.hasNext());

	// parameters required for this example to run
	if (!params.containsKey("indexPath")) {
	    System.err.println("Error: Parameters were missing.");
	    System.exit(1);
	}

	// open the index
	READER = DirectoryReader.open(FSDirectory.open(new File(params
		.get("indexPath"))));

	if (READER == null) {
	    System.err.println(usage);
	    System.exit(1);
	}
	dls = new DocLengthStore(READER);

	// read queries and store the query id - query string pair in hashmap
	HashMap<Integer, String> queries = new HashMap<Integer, String>();
	scan = new Scanner(new File(params.get("queryFilePath")));
	while (scan.hasNextLine()) {
	    line = scan.nextLine();
	    int commaIndex = line.indexOf(':');
	    int queryId = Integer.valueOf(line.substring(0, commaIndex).trim());
	    String queryString = line.substring(commaIndex + 1, line.length());
	    queries.put(queryId, queryString);
	}

	PrintStream out =  new PrintStream(new File(oFile));
	Parameter param = new Parameter();
	param.bm25_k1 = Float.valueOf(params.get("BM25:k_1"));
	param.bm25_b = Float.valueOf(params.get("BM25:b"));
	param.bm25_k3 = Float.valueOf(params.get("BM25:k_3"));
	param.indri_lambda = Float.valueOf(params.get("Indri:lambda"));
	param.indri_mu = Integer.valueOf(params.get("Indri:mu"));
	param.indri_smoothing = params.get("Indri:smoothing");
	// parse and evaluate each queries
	for (int queryId : queries.keySet()) {
	    String queryString = queries.get(queryId);
	    QueryParser parser = new QueryParser(queryString,
		    params.get("retrievalAlgorithm"), param);
	    Qryop query = parser.parse();
	    QryResult result = query.evaluate();
	    result.docScores.sort();
	    printResults(queryId, result, out);
	}
//	long endTime = System.currentTimeMillis();
//	System.out.println(endTime - startTime);
    }

    /**
     * Get the external document id for a document specified by an internal
     * document id. Ordinarily this would be a simple call to the Lucene index
     * reader, but when the index was built, the indexer added "_0" to the end
     * of each external document id. The correct solution would be to fix the
     * index, but it's too late for that now, so it is fixed here before the id
     * is returned.
     * 
     * @param iid
     *            The internal document id of the document.
     * @throws IOException
     */
    static String getExternalDocid(int iid) throws IOException {
	Document d = QryEval.READER.document(iid);
	String eid = d.get("externalId");

	if ((eid != null) && eid.endsWith("_0"))
	    eid = eid.substring(0, eid.length() - 2);

	return (eid);
    }

    /**
     * Prints the query results. The format is according to trec_eval
     * requirement: QueryID Q0 DocID Rank Score RunID
     * 
     * @param queryName
     *            Original query.
     * @param result
     *            Result object generated by {@link Qryop#evaluate()}.
     * @param out
     * @throws IOException
     */
    static void printResults(int queryId, QryResult result, PrintStream out)
	    throws IOException {
	for (int i = 0; i < result.docScores.scores.size() && i < 100; i++) {
	    out.printf("%d\tQ0\t%s\t%d\t%f\trun-1\n", queryId,
		    getExternalDocid(result.docScores.getDocid(i)), i + 1,
		    result.docScores.getDocidScore(i));
	}
    }

    /**
     * Given a query string, returns the terms one at a time with stopwords
     * removed and the terms stemmed using the Krovetz stemmer.
     * 
     * Use this method to process raw query terms.
     * 
     * @param query
     *            String containing query
     * @return Array of query tokens
     * @throws IOException
     */
    public static String[] tokenizeQuery(String query) throws IOException {

	TokenStreamComponents comp = analyzer.createComponents("dummy",
		new StringReader(query));
	TokenStream tokenStream = comp.getTokenStream();

	CharTermAttribute charTermAttribute = tokenStream
		.addAttribute(CharTermAttribute.class);
	tokenStream.reset();

	List<String> tokens = new ArrayList<String>();
	while (tokenStream.incrementToken()) {
	    String term = charTermAttribute.toString();
	    tokens.add(term);
	}
	return tokens.toArray(new String[tokens.size()]);
    }

}
