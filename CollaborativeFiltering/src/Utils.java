import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Utils {

    /**
     * read training data and populate rating into the utility matrix where
     * users are the rows and movies are columns
     * 
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    public static HashMap<Integer, User> readMatrixAsUsers(String file)
	    throws FileNotFoundException {
	HashMap<Integer, User> userHashtable = new HashMap<Integer, User>();
	Scanner scanner = new Scanner(new File(file));
	String line = null;
	line = scanner.nextLine(); // skip first line
	while (scanner.hasNext()) {
	    line = scanner.nextLine();
	    String token[] = line.split(",");
	    int movieId = Integer.parseInt(token[0]);
	    int userId = Integer.parseInt(token[1]);
	    float rating = Integer.parseInt(token[2]);
	    User user = null;
	    if (userHashtable.containsKey(userId)) {
		user = userHashtable.get(userId);
	    } else {
		user = new User(userId);
		userHashtable.put(userId, user);
	    }
	    user.addMovieRating(movieId, rating);
	}

	/* preprocess data */
	for (int key : userHashtable.keySet()) {
	    User user = userHashtable.get(key);
	    user.preprocess();
	    user.initialize();
	}

	return userHashtable;
    }

    /**
     * read training data and populate rating into the utility matrix where
     * movies are rows and users are columns
     * 
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    public static HashMap<Integer, Movie> readMatrixAsMovies(String file)
	    throws FileNotFoundException {
	HashMap<Integer, Movie> movieHashTable = new HashMap<Integer, Movie>();
	Scanner scanner = new Scanner(new File(file));
	String line = null;
	line = scanner.nextLine(); // skip first line
	while (scanner.hasNext()) {
	    line = scanner.nextLine();
	    String token[] = line.split(",");
	    int movieId = Integer.parseInt(token[0]);
	    int userId = Integer.parseInt(token[1]);
	    float rating = Integer.parseInt(token[2]);
	    Movie movie = null;
	    if (movieHashTable.containsKey(movieId)) {
		movie = movieHashTable.get(movieId);
	    } else {
		movie = new Movie(userId);
		movieHashTable.put(movieId, movie);
	    }
	    movie.addUserRating(userId, rating);
	}

	for (int key : movieHashTable.keySet()) {
	    Movie movie = movieHashTable.get(key);
	    movie.preprocess();
	    movie.initialize();
	}

	return movieHashTable;
    }

    /**
     * compute cosine similarity between two vectors
     * 
     * @param v1
     * @param v2
     * @return
     */
    public static float computeCosineSimilarity(HashMap<Integer, Float> v1,
	    float length1, HashMap<Integer, Float> v2, float length2) {
	if (length1 == 0 || length2 == 0)
	    return 0;
	else {
	    float dotProduct = computeDotProduct(v1, v2);
	    return dotProduct / (length1 * length2);
	}
    }

    /**
     * compute dot product for two vectors
     * 
     * @param vec1
     * @param vec2
     * @return
     */
    public static float computeDotProduct(HashMap<Integer, Float> vec1,
	    HashMap<Integer, Float> vec2) {
	float product = 0;
	for (int key : vec1.keySet()) {
	    if (vec2.containsKey(key)) {
		float val1 = vec1.get(key);
		float val2 = vec2.get(key);
		product += val1 * val2;
	    }
	}
	return product;
    }

    /**
     * compute l2-norm of a vector
     * 
     * @param vector
     * @return
     */
    public static float computeVectorLength(HashMap<Integer, Float> vector) {
	if (vector.isEmpty())
	    return 0;
	float length = 0;
	for (int key : vector.keySet()) {
	    float val = vector.get(key);
	    length += val * val;
	}

	return (float) Math.sqrt(length);
    }
}
