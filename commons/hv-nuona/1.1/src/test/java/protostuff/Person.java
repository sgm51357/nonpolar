package protostuff;

import java.util.Map;

public class Person {
	
	public int id;
	public String email;
	public Double doubleF;
	public Float floatF;
	public int[] ids;
	public byte a;
	public byte[] bytesF;
	public Boolean boolF;
	public Map<String, String> map;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public Double getDoubleF() {
		return doubleF;
	}
	
	public void setDoubleF(Double doubleF) {
		this.doubleF = doubleF;
	}
	
	public Float getFloatF() {
		return floatF;
	}
	
	public void setFloatF(Float floatF) {
		this.floatF = floatF;
	}
	
	public byte[] getBytesF() {
		return bytesF;
	}
	
	public void setBytesF(byte[] bytesF) {
		this.bytesF = bytesF;
	}
	
	public Boolean getBoolF() {
		return boolF;
	}
	
	public void setBoolF(Boolean boolF) {
		this.boolF = boolF;
	}
	
	public int[] getIds() {
		return ids;
	}
	
	public void setIds(int[] ids) {
		this.ids = ids;
	}
	
	public byte getA() {
		return a;
	}
	
	public Map<String, String> getMap() {
		return map;
	}
	
	public void setMap(Map<String, String> map) {
		this.map = map;
	}
	
	public void setA(byte a) {
		this.a = a;
	}
}
