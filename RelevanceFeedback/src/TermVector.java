/*
 *  Copyright (c) 2013, Carnegie Mellon University.  All Rights Reserved.
 *
 *  The TermVector class provides an Indri DocVector-style interface
 *  for the Lucene termvector.  There are three main data structurs:
 *    stems:	   	The field's vocabulary.  The 0'th entry is
 *			an empty string.  It indicates a stopword.
 *    stemsFreq:	The frequency (tf) of each entry in stems.
 *    positions:	The index of the stem that occurred at this
 *			position. 
 */

import java.util.*;
import java.io.*;

import org.apache.lucene.index.*;
import org.apache.lucene.util.*;
import org.apache.lucene.search.*;

public class TermVector {

  /**
   *  Class variables.
   */
  Terms luceneTerms;
  int[] positions;
  int positionsLength=0;
  String[] stems;
  int [] stemsFreq;
  int stemsLength=0;

  /**
   *  Constructor.  Create a TermVector for a field in a document.
   */
  public TermVector (int docId, String fieldName) throws IOException {

    /**
     *  Fetch the term vector.
     */
    this.luceneTerms = QryEval.READER.getTermVector (docId, fieldName);

    /**
     *  Allocate space for stems.  The 0'th stem indicates a stopword.
     */
    stemsLength = (int) this.luceneTerms.size ();
    stems = new String[stemsLength + 1];
    stemsFreq = new int[stemsLength + 1];

    /**
     *  Iterate through the terms, filling in the stem and frequency
     *  information, and finding the position of the last term.  The
     *  0'th term indicates a stopword, so this loop starts at i=1.
     */
    TermsEnum ithTerm = this.luceneTerms.iterator (null);

    for (int i=1; ithTerm.next() != null; i++) {
      stems[i] = ithTerm.term().utf8ToString();
      stemsFreq[i] = (int) ithTerm.totalTermFreq();

      /**
       *  Find the position of the last (indexed) term in the
       *  document, so that the positions array can be created and
       *  populated later.  The last position for each term is the
       *  largest, so ignore the positions before it.
       */
      DocsAndPositionsEnum ithPositions =
	ithTerm.docsAndPositions (null, null);

      ithPositions.nextDoc ();		/* Initialize iPositions */

      for (int j=0; j<ithPositions.freq()-1; j++)
	ithPositions.nextPosition();

      positionsLength = Math.max (positionsLength,
				  ithPositions.nextPosition());
    }

    /**
     *  Create and fill the positions array.  Note that the stems
     *  array uses stem 0 to indicate a stopword, so "real" stems have
     *  indexs 1 through length+1.
     */
    positions = new int[positionsLength + 1];

    ithTerm = this.luceneTerms.iterator (null);

    for (int i=0; ithTerm.next() != null; i++) {
      DocsAndPositionsEnum ithPositions =
	ithTerm.docsAndPositions (null, null);

      ithPositions.nextDoc ();		/* Initialize iPositions */

      for (int j=0; j<ithPositions.freq(); j++)
	positions[ithPositions.nextPosition()] = i+1;
    }
  }

  /**
   *  The number of positions in this field (the length of the field).
   *  If positions are not stored, it returns 0.
   */
  public int positionsLength() {
    return this.positionsLength;
  }

  /**
   *  Return the index of the stem that occurred at position i in the
   *  document.  If positions are not stored, it returns -1.
   */
  public int stemAt (int i) {
    if (i < positionsLength)
      return positions[i];
    else
      return -1;
  }

  /**
   *  The frequency of the n'th stem, or -1 if the index is invalid.
   *  The frequency for stopwords (i=0) is not stored (0 is returned).
   */
  public int stemFreq (int i) {
    if (i < stemsLength)
      return stemsFreq[i];
    else
      return -1;
  }  

  /**
   *  The string for the i'th stem, or null if the index is invalid.
   */
  public String stemString (int i) {
    if (i < stemsLength)
      return stems[i];
    else
      return null;
  }

  /**
   *  The number of unique stems in this field.
   */
  public int stemsLength () {
    return this.stemsLength;
  }

}