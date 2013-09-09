import java.io.IOException;


public class QryopNear extends Qryop {

  private int neighbor;
  
  public QryopNear(int n) {
    neighbor = n;
  }

  @Override
  public QryResult evaluate() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }
  
  public String toString() {
    StringBuffer buf = new StringBuffer("#Near/");
    buf.append(Integer.toString(neighbor));
    buf.append("(");
    for (int i = 0; i < args.size(); i++) {
      buf.append(args.get(i).toString());
      buf.append(i == args.size() - 1 ? ")" : " ");
    }
    return buf.toString();

  }

}
