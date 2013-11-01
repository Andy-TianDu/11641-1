
public interface ISimilarityMetric {
    public abstract float computeSimilarity(User u1, User u2);
    public abstract float computeSimilarity(Movie m1, Movie m2);
}
