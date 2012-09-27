package eu.fisoft.fit.edux;

/**
 * Auth methods available on edux page
 * @author fifiksta
 *
 */
public enum AuthMethod {

	EDUX ("Externisté (EDUX)",         0), 
	SUN  ("Server SUN FIT ÈVUT",       2),
	FIT  ("FIT ÈVUT - fakultní heslo", 3),
	KOS  ("heslo KOS, usermap",        4);
	
	private String name;
	private int id;
	private AuthMethod(String name, int id) {
		this.name = name;
		this.id = id;
	}
	
	public String toString() {
		return name;
	};
	
	public int getAuthID(){
		return id;
	}
	
}
