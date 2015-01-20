package protostuff;

import java.lang.reflect.Field;

/**
 * @author shanguoming 2014年12月30日 下午3:24:05
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年12月30日 下午3:24:05
 */
public class User extends Person {
	
	private String password;
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public static void main(String[] args) {
		Class<?> targetClass = User.class;
		do {
			Field[] fields = targetClass.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				System.out.println(fields[i].getName());
			}
			targetClass = targetClass.getSuperclass();
		} while (null != targetClass && targetClass != Object.class);
    }
}
