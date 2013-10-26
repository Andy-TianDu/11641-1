import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class User {
    private int userId;
    HashMap<Integer, Float> movieRatings = new HashMap<Integer, Float>();
    private float totalRating = 0;
    private float avg_rating = 0;
    // HashMap<Integer, Float> similarityCache = new HashMap<Integer, Float>();
    // it seems that similarity cache would become a burden in heap space
    private float vecLength;
    private List<Similarity> nearestNeighbors = null;

    public User(int userId) {
	this.userId = userId;
    }

    // TODO
    public void addMovieRating(int movieId, Float rating) {
	// no duplicate ratings
	movieRatings.put(movieId, rating);
	totalRating += rating;
    }

    public void initialize() {
	vecLength = Utils.computeVectorLength(movieRatings);
    }

    /**
     * preprocess the utility matrix so that each cell is deducted by the mean
     * rating for this user the empty cell remains empty, representing an
     * average score for unrated movie
     */
    public void preprocess() {
	float new_avg_rating = totalRating / movieRatings.size();
	for (int key : movieRatings.keySet()) {
	    float rating = movieRatings.get(key);
	    movieRatings.put(key, rating + avg_rating - new_avg_rating);
	}
	avg_rating = new_avg_rating;
    }

    /**
     * compute similarity score between this user and u2 first check cache to
     * see if the similarity is already computed if cache hit, return the score
     * if miss, recompute, cache the result, then return the similarity score
     * 
     * @param u2
     * @return
     */
    public float computeSimilarity(User u2) {
	float similarity = 0;

	// check both caches to see if there is a match
	// if (similarityCache.containsKey(u2.userId)) {
	// similarity = similarityCache.get(u2.userId);
	// }
	// if (u2.similarityCache.containsKey(userId)) {
	// similarity = u2.similarityCache.get(userId);
	// } else {
	similarity = Utils.computeCosineSimilarity(movieRatings, vecLength,
		u2.movieRatings, u2.vecLength);
	// similarityCache.put(u2.userId, similarity);
	// }
	return similarity;
    }

    /**
     * find k nearest users
     * @return
     */
    public List<Similarity> findKNearestNeighibors(int k) {
	if (nearestNeighbors != null) {
	    // System.out.println("hit");
	    return nearestNeighbors;
	} else {
	    // first compute all similarities
	    PriorityQueue<Similarity> userSimilarities = new PriorityQueue<Similarity>();
	    for (int uid : Recommender.users.keySet()) {
		if (uid != userId) {
		    User user = Recommender.users.get(uid);
		    float simScore = computeSimilarity(user);
		    userSimilarities.add(new Similarity(uid, simScore));
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
     * if already contains rating: return rating for this movie else: return
     * avg_rating
     * 
     * @param movieId
     * @return
     */
    public float getPseudoMovieRating(int movieId) {
	if (movieRatings.containsKey(movieId)) {
	    return movieRatings.get(movieId) + avg_rating;
	} else {
	    return avg_rating;
	}
    }

    public int hashCode() {
	return new Integer(userId).hashCode();
    }

    @Override
    public boolean equals(Object o) {
	User u = (User) o;
	return u.userId == userId;
    }
}
