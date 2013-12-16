package serverPackage;

import java.util.EventObject;

public class ResponseEvent extends EventObject{
	
	public String id;
	public boolean response;

	public ResponseEvent(Object source, String id, boolean response) {
		super(source);
		this.id =id;
		this.response = response;
	}

	public String getId(){
		return id;
	}
	
	public boolean getResponse(){
		return response;
	}
	
	
}
