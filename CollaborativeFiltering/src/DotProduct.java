
public class DotProduct implements ISimilarityMetric {

    @Override
    public float computeSimilarity(User u1, User u2) {
	return Utils.computeDotProduct(u1.movieRatings, u2.movieRatings);
    }

    @Override
    public float computeSimilarity(Movie m1, Movie m2) {
	return Utils.computeDotProduct(m1.userRatings, m2.userRatings);
    }
    
    @Override
    public String toString() {
	return "Dot Product";
    }
    
}
