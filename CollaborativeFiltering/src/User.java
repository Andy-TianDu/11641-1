import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class User {
    int userId;
    HashMap<Integer, Float> movieRatings = new HashMap<Integer, Float>();
    private float totalRating = 0;
    private float avg_rating = 0;
    private float prior_term = 0;
    private float std = 1;
    // HashMap<Integer, Float> similarityCache = new HashMap<Integer, Float>();
    // it seems that similarity cache would become a burden in heap space
    float vecLength;
    private List<Neighbor> nearestNeighbors = null;

    public User(int userId) {
	this.userId = userId;
    }

    public void addMovieRating(int movieId, Float rating) {
	// no duplicate ratings
	movieRatings.put(movieId, rating);
	totalRating += rating;
    }

    /**
     * preprocess the utility matrix so that each cell is deducted by the mean
     * rating for this user the empty cell remains empty, representing an
     * average score for unrated movie
     */
    public void preprocess(boolean normalization) {
	float new_avg_rating = totalRating / movieRatings.size();
	if (normalization) {
	    prior_term = new_avg_rating;
	} else {
	    prior_term = 3;
	}
	for (int key : movieRatings.keySet()) {
	    float rating = movieRatings.get(key);
	    movieRatings.put(key, rating - prior_term);
	}
	avg_rating = new_avg_rating;
	vecLength = Utils.computeVectorLength(movieRatings);
	if (normalization) {
	    prior_term = new_avg_rating;
	    if (vecLength != 0) {
		std = vecLength;
		for (int key : movieRatings.keySet()) {
		    float rating = movieRatings.get(key);
		    movieRatings.put(key, rating / std);
		}
		vecLength = 1;
	    }
	} else {
	    std = 1;
	}
    }

    /**
     * find k nearest users
     * 
     * @return
     */
    public List<Neighbor> findKNearestNeighibors(int k,
	    ISimilarityMetric metric, boolean useCache, boolean useMean) {
	if (useCache && nearestNeighbors != null) {
	    // System.out.println("hit");
	    return nearestNeighbors;
	} else {
	    // first compute all similarities
	    PriorityQueue<Neighbor> userSimilarities = new PriorityQueue<Neighbor>();
	    for (int uid : Recommender.users.keySet()) {
		if (uid != userId) {
		    User user = Recommender.users.get(uid);
		    float simScore = metric.computeSimilarity(this, user);
		    if (userSimilarities.size() < k)
			userSimilarities.add(new Neighbor(uid, simScore));
		    else if (userSimilarities.peek().simScore < simScore
			    || (userSimilarities.peek().simScore == simScore && userSimilarities
				    .peek().id > uid)) {
			userSimilarities.poll();
			userSimilarities.add(new Neighbor(uid, simScore));
		    }
		}
	    }

	    // sort neighbors by similarity score
	    List<Neighbor> neighbors = new ArrayList<Neighbor>();

	    // normalize weight
	    for (int i = 0; i < k; i++) {
		neighbors.add(userSimilarities.poll());
	    }
	    float totalSim = 0;
	    for (Neighbor sim : neighbors) {
		// System.out.printf("%f ", sim.simScore);
		sim.simScore = (sim.simScore + 1) / 2; // rescale to 0...1
		totalSim += sim.simScore;
	    }
	    // System.out.println();
	    for (Neighbor sim : neighbors) {
		if (useMean || totalSim == 0)
		    sim.simScore = 1.0f / k;
		else
		    sim.simScore /= totalSim;
	    }
	    Collections.reverse(neighbors);
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
	    return movieRatings.get(movieId) * std + prior_term;
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
