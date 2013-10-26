import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class Movie {
    private int movieId;
    private HashMap<Integer, Float> userRatings = new HashMap<Integer, Float>();
    private float totalRating = 0;
    private float avg_rating = 0;

    private float vecLength;
    List<Similarity> nearestNeighbors;

    public Movie(int movieId) {
	this.movieId = movieId;
    }

    // TODO
    public void addUserRating(int userId, Float rating) {
	// no duplicate ratings
	userRatings.put(userId, rating);
	totalRating += rating;
    }

    public List<Similarity> findKNearestNeighibors(int k) {
	if (nearestNeighbors != null) {
	    // System.out.println("hit");
	    return nearestNeighbors;
	} else {
	    // first compute all similarities
	    PriorityQueue<Similarity> userSimilarities = new PriorityQueue<Similarity>();
	    for (int mid : Recommender.movies.keySet()) {
		if (mid != movieId) {
		    Movie movie = Recommender.movies.get(mid);
		    float simScore = computeSimilarity(movie);
		    userSimilarities.add(new Similarity(mid, simScore));
		}
	    }

	    // sort neighbors by similarity score
	    List<Similarity> neighbors = new ArrayList<Similarity>();
	    float minSim = Float.MAX_VALUE;

	    // normalize weight
	    for (int i = 0; i < k; i++) {
		minSim = Math.min(minSim, userSimilarities.peek().simScore);
		neighbors.add(userSimilarities.poll());
	    }
	    float totalSim = 0;
	    for (Similarity sim : neighbors) {
		sim.simScore -= minSim;
		totalSim += sim.simScore;
	    }
	    for (Similarity sim : neighbors) {
		if (totalSim == 0)
		    sim.simScore = 1.0f / k;
		else
		    sim.simScore /= totalSim;
	    }
	    nearestNeighbors = neighbors;
	    return nearestNeighbors;
	}
    }

    /**
     * if already contains rating: return rating from this user 
     * else: return avg_rating
     * 
     * @param movieId
     * @return
     */
    public float getPseudoUserRating(int userId) {
	if (userRatings.containsKey(userId)) {
	    return userRatings.get(userId) + avg_rating;
	} else {
	    return avg_rating;
	}
    }

    /**
     * preprocess the utility matrix so that each cell is deducted by the mean
     * rating for this user the empty cell remains empty, representing an
     * average score for unrated movie
     */
    public void preprocess() {
	float new_avg_rating = totalRating / userRatings.size();
	for (int key : userRatings.keySet()) {
	    float rating = userRatings.get(key);
	    userRatings.put(key, rating + avg_rating - new_avg_rating);
	}
	avg_rating = new_avg_rating;
    }

    public float computeSimilarity(Movie m2) {
	float similarity = 0;
	similarity = Utils.computeCosineSimilarity(userRatings, vecLength,
		m2.userRatings, m2.vecLength);
	return similarity;
    }

    public int hashCode() {
	return new Integer(movieId).hashCode();
    }

    @Override
    public boolean equals(Object o) {
	Movie u = (Movie) o;
	return u.movieId == movieId;
    }

    public void initialize() {
	vecLength = Utils.computeVectorLength(userRatings);
    }

}
