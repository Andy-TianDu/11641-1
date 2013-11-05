import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Recommender {

	public static HashMap<Integer, User> users;
	public static HashMap<Integer, Movie> movies;
	public static PrintStream outStream;
	public static ISimilarityMetric metric = new DotProduct();
	public static boolean useCache = true;
	public static boolean useMean = true;
	public static boolean normalization = false;
	public static int k = 10;
	public static Strategy strategy = Strategy.item_based;
	public static String strtg = "item_based";

	public static void predictRating(Query query) {
		float prediction = 0;
		if (strategy == Strategy.user_based) {
			User user = users.get(query.userId);
			List<Neighbor> neighbors = user.findKNearestNeighibors(k, metric,
					useCache, useMean);
			for (Neighbor neighbor : neighbors) {
				// System.out.println(neighbor);
				User neighbor_u = users.get(neighbor.id);
				float rating = neighbor_u.getPseudoMovieRating(query.movieId);
				prediction += rating * neighbor.simScore;
			}
		} else {
			Movie movie = movies.get(query.movieId);
			List<Neighbor> neighbors = movie.findKNearestNeighibors(k, metric,
					useCache, useMean);
			for (Neighbor neighbor : neighbors) {

				Movie neighbor_m = movies.get(neighbor.id);
				float rating = neighbor_m.getPseudoUserRating(query.userId);
				// System.out.printf("%s %f\n", neighbor, rating);
				prediction += rating * neighbor.simScore;
			}
		}
		// System.out.println(prediction);
		query.rating = prediction;
	}

	public static void main(String[] argv) throws Exception {
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

		/* read parameters */
		try {
			String line = null;
			HashMap<String, String> params = new HashMap<String, String>();
			Scanner scanner = new Scanner(new File("resources/params.txt"));
			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				String pair[] = line.split(":");
				params.put(pair[0].trim(), pair[1].trim());
			}
			normalization = Boolean.parseBoolean(params.get("normalization"));
			useMean = Boolean.parseBoolean(params.get("useMean"));
			k = Integer.parseInt(params.get("k"));
			metric = (ISimilarityMetric) Class
					.forName(params.get("similarity")).newInstance();
			strtg = params.get("strategy");
		} catch (Exception exception) {
		}
		if (strtg.equals("user_based")) {
			users = Utils.readMatrixAsUsers(trainFile);
			Utils.preprocessUsers(users, normalization);
			strategy = Strategy.user_based;
		} else {
			movies = Utils.readMatrixAsMovies(trainFile);
			Utils.preprocessMovies(movies, normalization);
			strategy = Strategy.item_based;
		}
		outStream = new PrintStream(new File(outputFile));
		long startTime = System.currentTimeMillis();
		int i = 0;
		List<Query> queries = Utils.readQueries(testFile);
		for (Query query : queries) {
			if (++i % 1000 == 0) {
				long currentTime = System.currentTimeMillis();
//				System.out.printf("%d query processed\nTime spent:%d\n", i,
//						currentTime - startTime);
			}
			predictRating(query);
		}
		for (Query query : queries) {
			outStream.println(query);
		}
		long currentTime = System.currentTimeMillis();
//		System.out.println((currentTime - startTime) / 1000);
	}
}