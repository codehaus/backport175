package fluffy;

public class Base {

	public static void onlyBase() {}
	public static void both() {}

	public int onlyBase;
	public int both;
	
	public Base() {}
	public Base(int i) {}
	
	public void m() throws CloneNotSupportedException {}
}
