package gopher;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexURI implements Serializable, Cloneable {
	public final static String regexQuick = "^(?:(?:(?<scheme>[a-z]+):)?\\/+)?(?:(?<user>[^\\s:]+)(?::(?<pass>[^\\s]+))?@)?(?<host>(?:[^\\s\\/:?#.]+\\.)*(?:[^\\d\\s\\/:?#.-]+|xn--[a-z0-9]+)|\\d+(?:\\.\\d+){3}|[0-9a-f]{0,4}:(?:[0-9a-f]{0,4}:){7})(?::(?<port>\\d+))?(?<path>\\/[^:?#]*)?(?:\\?(?<query>[^#]*))?(?:#(?<frag>[^]*))?$";
	public final static String regexExact = "^(?:(?:(?<scheme>[a-z]+):)?\\/+)?(?:(?<user>[^\\s:]+)(?::(?<pass>[^\\s]+))?@)?(?<host>(?:[^\\s\\/:?#.]+\\.)*(?:[^\\d\\s\\/:?#.-]+|xn--[a-z0-9]+)|(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|[0-9a-f]{0,4}:(?:[0-9a-f]{0,4}:){7})(?::(?<port>[0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5]))?(?<path>\\/[^:?#]*)?(?:\\?(?<query>[^#]*))?(?:#(?<frag>.*))?$";
	
	public final String scheme;
	public final String user;
	public final String pass;
	public final String host;
	public final int    port;
	public final String path;
	public final String query;
	public final String frag;
	
	protected final static Pattern regexPattern = Pattern.compile(regexExact);
	
	public RegexURI(String input) throws URISyntaxException {
		Matcher matcher = regexPattern.matcher(input);
		if (matcher.find()) {
			scheme = matcher.group("scheme");
			user   = matcher.group("user");
			pass   = matcher.group("pass");
			host   = matcher.group("host");
			port   = matcher.group("port") != null ? Integer.parseInt(matcher.group("port")) : -1;
			path   = matcher.group("path");
			query  = matcher.group("query");
			frag   = matcher.group("frag");
		} else throw new URISyntaxException(input, "missing");
	}

	public RegexURI(String scheme, String user, String pass, String host, int port, String path,
	    String query, String frag) {
		this.scheme = scheme;
		this.user   = user;
		this.pass   = pass;
		this.host   = host;
		this.port   = port;
		this.path   = path;
		this.query  = query;
		this.frag   = frag;
	}

	public String   getScheme () {return scheme;}
	public String   getUser   () {return user;  }
	public String   getPass   () {return pass;  }
	public String   getHost   () {return host;  }
	public int      getPort   () {return port;  }
	public String   getPath   () {return path;  }
	public String   getQuery  () {return query; }
	public String   getFrag   () {return frag;  }

	public RegexURI setScheme(String scheme) {return new RegexURI(scheme, user, pass, host, port, path, query, frag);}
	public RegexURI setUser  (String user  ) {return new RegexURI(scheme, user, pass, host, port, path, query, frag);}
	public RegexURI setPass  (String pass  ) {return new RegexURI(scheme, user, pass, host, port, path, query, frag);}
	public RegexURI setHost  (String host  ) {return new RegexURI(scheme, user, pass, host, port, path, query, frag);}
	public RegexURI setPort  (int    port  ) {return new RegexURI(scheme, user, pass, host, port, path, query, frag);}
	public RegexURI setPath  (String path  ) {return new RegexURI(scheme, user, pass, host, port, path, query, frag);}
	public RegexURI setQuery (String query ) {return new RegexURI(scheme, user, pass, host, port, path, query, frag);}
	public RegexURI setFrag  (String frag  ) {return new RegexURI(scheme, user, pass, host, port, path, query, frag);}

	@Override
	public Object clone() {
		return new RegexURI(scheme, user, pass, host, port, path, query, frag);
	}
	
	public RegexURI apply(RegexURI that) {
		return new RegexURI(
			that.scheme != null ? that.scheme : this.scheme,
			that.user   != null ? that.user   : this.user,
			that.pass   != null ? that.pass   : this.pass,
			that.host   != null ? that.host   : this.host,
			that.port   >  0    ? that.port   : this.port,
			that.path   != null ? that.path   : this.path,
			that.query  != null ? that.query  : this.query,
			that.frag   != null ? that.frag   : this.frag
		);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		if (scheme != null) builder.append(scheme).append("://");
		if (user   != null) {
			builder.append(user);
			if (pass != null) builder.append(":").append(pass);
			builder.append("@");
		}
		builder.append(host);
		if (port   >  0   ) builder.append(":").append(port);
		if (path   != null) builder.append(path);
		if (query  != null) builder.append("?").append(query);
		if (frag   != null) builder.append("#").append(frag);
		
		return builder.toString();
	}
}
