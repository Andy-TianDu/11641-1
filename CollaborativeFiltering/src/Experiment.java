import java.io.FileNotFoundException;
import java.util.HashMap;

public class Experiment {
    static HashMap<Integer, User> users;
    static HashMap<Integer, Movie> movies;
    static ISimilarityMetric dotProduct = new DotProduct();
    static ISimilarityMetric cosineSimilarity = new CosineSimilarity();
    static {
	try {
	    users = Utils.readMatrixAsUsers("resources/training_set.csv");
	    movies = Utils.readMatrixAsMovies("resources/training_set.csv");
	    Recommender.users = users;
	    Recommender.movies = movies;
	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
    }
    
    public static void main(String args[]) throws FileNotFoundException {
	System.out.printf("number of users: %d\n", users.size());
	System.out.printf("number of movies: %d\n", movies.size());
	int num1, num3, num5, num_rating, totalRating;
	float average_rating = 0;
	num1 = num3 = num5 = num_rating = totalRating = 0;
	for (int userId : users.keySet()) {
	    User user = users.get(userId);
	    for (int movieId : user.movieRatings.keySet()) {
		float rating = user.movieRatings.get(movieId);
		if (rating == 1)
		    num1++;
		else if (rating == 3)
		    num3++;
		else if (rating == 5)
		    num5++;
		totalRating += rating;
		num_rating++;
	    }
	}
	average_rating = 1.0f * totalRating / num_rating; 
	System.out.printf("number of movies rated 1:%d\n", num1);
	System.out.printf("number of movies rated 3:%d\n", num3);
	System.out.printf("number of movies rated 5:%d\n", num5);
	System.out.printf("average rating %f\n", average_rating);
	user_stat(1234576);
	movie_stat(4321);
	Utils.preprocessMovies(Recommender.movies, true);
	Utils.preprocessUsers(Recommender.users, true);
	System.out.println(Recommender.users.get(1687642).movieRatings);
	knn(5, users.get(303969), dotProduct);
	System.out.println(dotProduct.computeSimilarity(Recommender.users.get(303969), Recommender.users.get(1687642)));
	System.out.println(Recommender.users.get(303969).movieRatings);
	knn(5, users.get(1234576), dotProduct);
	knn(5, users.get(1234576), cosineSimilarity);
	knn(5, movies.get(4321), dotProduct);
	knn(5, movies.get(4321), cosineSimilarity);
    }
    
    public static void user_stat(int user_id) {
	User user = users.get(user_id);
	int num1, num3, num5, num_rating, totalRating;
	float average_rating = 0;
	num1 = num3 = num5 = num_rating = totalRating = 0;
	for (int movie_id : user.movieRatings.keySet()) {
	    float rating = user.movieRatings.get(movie_id);
	    if (rating == 1)
		    num1++;
		else if (rating == 3)
		    num3++;
		else if (rating == 5)
		    num5++;
		totalRating += rating;
		num_rating++;
	}
	average_rating = 1.0f * totalRating / num_rating;
	System.out.printf("number of movies rated for user %d:%d\n",user_id, user.movieRatings.size());
	System.out.printf("number of movies rated 1 for user %d:%d\n", user_id, num1);
	System.out.printf("number of movies rated 3 for user %d:%d\n", user_id, num3);
	System.out.printf("number of movies rated 5 for user %d:%d\n", user_id, num5);
	System.out.printf("average rating %f\n", average_rating);

    }
    
    public static void movie_stat(int movie_id) {
	Movie movie = movies.get(movie_id);
	int num1, num3, num5, num_rating, totalRating;
	float average_rating = 0;
	num1 = num3 = num5 = num_rating = totalRating = 0;
	for (int user_id : movie.userRatings.keySet()) {
	    float rating = movie.userRatings.get(user_id);
	    if (rating == 1)
		    num1++;
		else if (rating == 3)
		    num3++;
		else if (rating == 5)
		    num5++;
		totalRating += rating;
		num_rating++;
	}
	average_rating = 1.0f * totalRating / num_rating;
	System.out.printf("number of users rated for movie %d:%d\n", movie_id, movie.userRatings.size());
	System.out.printf("number of movies rated 1 for movie %d:%d\n", movie_id, num1);
	System.out.printf("number of movies rated 3 for movie %d:%d\n", movie_id, num3);
	System.out.printf("number of movies rated 5 for movie %d:%d\n", movie_id, num5);
	System.out.printf("average rating %f\n", average_rating);
    }

    public static void knn(int k, User user, ISimilarityMetric metric) {
	System.out.println(k + "nearest neighbors of user " + user.userId);
	System.out.println(metric);
	for (Neighbor neighbor: user.findKNearestNeighibors(k, metric, false, false)) {
	    System.out.printf("%d ", neighbor.id);
	}
	System.out.println();
    }
    
    public static void knn(int k, Movie movie, ISimilarityMetric metric) {
 	System.out.println(k + "nearest neighbors of movie " + movie.movieId);
 	System.out.println(metric);
 	for (Neighbor neighbor: movie.findKNearestNeighibors(k, metric, false, false)) {
 	    System.out.printf("%d ", neighbor.id);
 	}
 	System.out.println();
     }
    
}
