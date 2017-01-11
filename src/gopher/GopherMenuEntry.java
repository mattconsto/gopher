package gopher;

public class GopherMenuEntry {
	public final char     type;
	public final String   message;
	public final RegexURI uri;
	
	public GopherMenuEntry(char type, String message, RegexURI uri) {
		this.type    = type;
		this.message = message;
		this.uri     = uri;
	}
	
	@Override
	public String toString() {
		return message;
	}
}
