
/**
 * stores parameters of retrieval models
 * @author Siping Ji <sipingji@cmu.edu>
 *
 */
public class Parameter {
    public float bm25_k1;
    public float bm25_b;
    public float bm25_k3;
    public int indri_mu;
    public float indri_lambda;
    public String indri_smoothing;
    public int fb_numDocs;
    public int fb_numTerms;
    public int fb_mu;
    public float fb_originalWeight;
    public boolean fb;
    public String fb_file;
    
    public Parameter() {}
}
