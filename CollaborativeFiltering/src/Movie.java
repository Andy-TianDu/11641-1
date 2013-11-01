import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class Movie {
    int movieId;
    public HashMap<Integer, Float> userRatings = new HashMap<Integer, Float>();
    private float totalRating = 0;
    private float avg_rating = 0;
    private float prior_term = 0;
    private float std = 1;

    float vecLength;
    List<Neighbor> nearestNeighbors;

    public Movie(int movieId) {
	this.movieId = movieId;
    }

    public void addUserRating(int userId, Float rating) {
	// no duplicate ratings
	userRatings.put(userId, rating);
	totalRating += rating;
    }

    public List<Neighbor> findKNearestNeighibors(int k,
	    ISimilarityMetric metric, boolean useCache, boolean useMean) {
	if (useCache && nearestNeighbors != null) {
	    // System.out.println("hit");
	    return nearestNeighbors;
	} else {
	    // first compute all similarities
	    PriorityQueue<Neighbor> movieSimilarities = new PriorityQueue<Neighbor>();
	    for (int mid : Recommender.movies.keySet()) {
		if (mid != movieId) {
		    Movie movie = Recommender.movies.get(mid);
		    float simScore = metric.computeSimilarity(this, movie);
		    if (movieSimilarities.size() < k)
			movieSimilarities.add(new Neighbor(mid, simScore));
		    else if (movieSimilarities.peek().simScore < simScore
			    || (movieSimilarities.peek().simScore == simScore && movieSimilarities
				    .peek().id > mid)) {
			movieSimilarities.poll();
			movieSimilarities.add(new Neighbor(mid, simScore));
		    }
		}
	    }

	    // sort neighbors by similarity score
	    List<Neighbor> neighbors = new ArrayList<Neighbor>();

	    // normalize weight
	    for (int i = 0; i < k; i++) {
		neighbors.add(movieSimilarities.poll());
	    }
	    float totalSim = 0;
	    for (Neighbor sim : neighbors) {
//		System.out.printf("%f ", sim.simScore);
		sim.simScore = (sim.simScore + 1) / 2; // rescale to 0...1
		totalSim += sim.simScore;
	    }
//	    System.out.println();
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
     * if already contains rating: return rating from this user else: return
     * avg_rating
     * 
     * @param movieId
     * @return
     */
    public float getPseudoUserRating(int userId) {
	if (userRatings.containsKey(userId)) {
	    return userRatings.get(userId) * std + prior_term;
	} else {
	    return avg_rating;
	}
    }

    /**
     * preprocess the utility matrix so that each cell is deducted by the mean
     * rating for this user the empty cell remains empty, representing an
     * average score for unrated movie
     * 
     * @param normalization
     */
    public void preprocess(Boolean normalization) {
	float new_avg_rating = totalRating / userRatings.size();
	if (normalization) {
	    prior_term = new_avg_rating;
	} else {
	    prior_term = 3;
	}
	for (int key : userRatings.keySet()) {
	    float rating = userRatings.get(key);
	    userRatings.put(key, rating - prior_term);
	}
	avg_rating = new_avg_rating;
	vecLength = Utils.computeVectorLength(userRatings);
	if (normalization) {
	    prior_term = new_avg_rating;
	    if (vecLength != 0) {
		std = vecLength;
		for (int key : userRatings.keySet()) {
		    float rating = userRatings.get(key);
		    userRatings.put(key, rating / std);
		}
		vecLength = 1;
	    }
	} else {
	    std = 1;
	}
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
