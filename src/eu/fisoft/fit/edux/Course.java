package eu.fisoft.fit.edux;

public class Course {

	private final String code;
	private final String name;
	private final String url;
	
	public Course(String code, String name, String url) {
		super();
		this.code = code;
		this.name = name;
		this.url = url;
	}
	
	
	public String getCode() {
		return code;
	}
	public String getName() {
		return name;
	}
	public String getUrl() {
		return url;
	}

}
