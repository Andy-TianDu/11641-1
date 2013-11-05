
public class EuclidianSimilarity implements ISimilarityMetric{

    @Override
    public float computeSimilarity(User u1, User u2) {
	return 1.0f / (1 + Utils.computeEuclidianDistance(u1.movieRatings, u2.movieRatings));
    }

    @Override
    public float computeSimilarity(Movie m1, Movie m2) {
	// TODO Auto-generated method stub
	return 1.0f / (1 + Utils.computeEuclidianDistance(m1.userRatings, m2.userRatings));
    }
    
}
