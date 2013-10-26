public class Similarity implements Comparable<Similarity>{
    public int id; // movie Id or user Id, depends on using item-item or
		    // user-user collaborative filtering
    public float simScore;
    
    public Similarity(int id, float score) {
	this.id = id;
	this.simScore = score;
    }
    @Override
    public int compareTo(Similarity o) {
	if (o.simScore > simScore)
	    return 1;
	else if (o.simScore < simScore)
	    return -1;
	return 0;
    }
}
