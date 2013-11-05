
public class CosineSimilarity implements ISimilarityMetric{

    @Override
    public float computeSimilarity(User u1, User u2) {
	return Utils.computeCosineSimilarity(u1.movieRatings, u1.vecLength, u2.movieRatings, u2.vecLength);
    }

    @Override
    public float computeSimilarity(Movie m1, Movie m2) {
	return Utils.computeCosineSimilarity(m1.userRatings, m1.vecLength, m2.userRatings, m2.vecLength);
    }
    
    @Override
    public String toString() {
	return "Cosine Similarity";
    }

}
