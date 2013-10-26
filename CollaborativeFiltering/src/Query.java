public class Query {
    int userId;
    int movieId;
    float rating;

    public Query(int userId, int movieId) {
	this.userId = userId;
	this.movieId = movieId;
    }
    
    @Override
    public String toString() {
	return new Float(rating).toString();
    }
}
