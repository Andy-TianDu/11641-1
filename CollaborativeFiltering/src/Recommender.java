import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Recommender {

    public static HashMap<Integer, User> users;
    public static HashMap<Integer, Movie> movies;
    public static PrintStream outStream;

    public static void predictRating(int userId, int movieId, Strategy strategy) {
	float prediction = 0;
	int k = 5;
	if (strategy == Strategy.user_based) {
	    User user = users.get(userId);
	    List<Similarity> neighbors = user.findKNearestNeighibors(k);
	    for (Similarity neighbor : neighbors) {
		User neighbor_u = users.get(neighbor.id);
		float rating = neighbor_u.getPseudoMovieRating(movieId);
		prediction += rating * neighbor.simScore;
	    }
	} else {
	    Movie movie = movies.get(movieId);
	    List<Similarity> neighbors = movie.findKNearestNeighibors(k);
	    for (Similarity neighbor : neighbors) {
		Movie neighbor_m = movies.get(neighbor.id);
		float rating = neighbor_m.getPseudoUserRating(userId);
		prediction += rating * neighbor.simScore;
	    }
	}
	outStream.println(prediction);
    }

    public static void main(String[] argv) throws FileNotFoundException {
	String trainFile = argv[0];
	String testFile = argv[1];
	String outputFile = argv[2];

	// If you see these outputs, it means you have successfully compiled and
	// run the code.
	// Then you can remove these three lines if you want.
	System.out.println("Training File : " + trainFile);
	System.out.println("Test File : " + testFile);
	System.out.println("Output File : " + outputFile);

	// Implement your recommendation modules using trainFile and testFile.
	// And output the prediction scores to outputFile.

//	users = Utils.readMatrixAsUsers(trainFile);
	movies = Utils.readMatrixAsMovies(trainFile);
	outStream = System.out;// new PrintStream(new File(outputFile));
	System.out.println(movies.size());
	Scanner scanner = new Scanner(new File(testFile));
	String line = null;
	line = scanner.nextLine(); // skip first line
	int i = 0;
	
	while (scanner.hasNext()) {
	    line = scanner.nextLine();
	    String token[] = line.split(",");
	    int movieId = Integer.parseInt(token[0]);
	    int userId = Integer.parseInt(token[1]);
	    System.out.printf("%s:  ", ++i);
	    predictRating(userId, movieId, Strategy.item_based);
	}

    }

}