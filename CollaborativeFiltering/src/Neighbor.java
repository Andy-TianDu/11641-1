public class Neighbor implements Comparable<Neighbor>{
    public int id; // movie Id or user Id, depends on using item-item or
		    // user-user collaborative filtering
    public float simScore;
    
    public Neighbor(int id, float score) {
	this.id = id;
	this.simScore = score;
    }
    @Override
    public int compareTo(Neighbor o) {
	if (simScore < o.simScore)
	    return -1;
	else if (simScore > o.simScore)
	    return 1;
	return o.id - this.id;
    }
    
    @Override
    public String toString() {
	return id + "\t" + simScore;
    }
}
