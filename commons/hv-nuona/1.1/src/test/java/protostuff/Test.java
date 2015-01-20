package protostuff;

import io.protostuff.LinkedBuffer;
import io.protostuff.Pipe;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.MappedSchema;
import io.protostuff.runtime.RuntimeSchema;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.ClassUtils;

/**
 * @author shanguoming 2014年12月30日 下午3:19:18
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年12月30日 下午3:19:18
 */
public class Test {
	
	public static void main(String[] args) throws IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		// // //类的模式设置为Person类
		Schema<Person> personSchema = RuntimeSchema.getSchema(Person.class);
		MappedSchema<Person> shcema = (MappedSchema<Person>)personSchema;
		Field[] fields = shcema.getClass().getSuperclass().getDeclaredFields();
		fields[1].setAccessible(true);
		MappedSchema.Field<?>[] fieldxs = (MappedSchema.Field<?>[])fields[1].get(shcema);
		for (MappedSchema.Field<?> f : fieldxs) {
			if (f.type.toString().equals("MESSAGE")) {
				
			} else {
				System.out.println((f.repeated?"repeated ":"optional ") + f.type.toString() + " " + f.name + " " + f.number+";");
			}
		}
		// for(Field f : fields){
		// f.setAccessible(true);
		// MappedSchema.Field<?> fields[] = f.get("")
		// System.out.println(f.getName());
		// }
		Person person1 = new Person();
		person1.setId(10086);
		int[] a = {1, 2};
		person1.setIds(a);
		Map<String, String> map = new HashMap<String, String>();
		map.put("abc", "aaa");
		person1.setEmail("ken@iamcoding.com");
		person1.setMap(map);
		// 缓存buff
		LinkedBuffer buffer = LinkedBuffer.allocate(1024);
		// 序列化成protobuf的二进制数据
		byte[] data = ProtobufIOUtil.toByteArray(person1, personSchema, buffer);
		// 反序列化
		User person2 = new User();
		Schema<User> userSchema = RuntimeSchema.getSchema(User.class);
		System.out.println(userSchema.messageFullName());
		ProtobufIOUtil.mergeFrom(data, person2, userSchema);
		System.out.println(person2.getMap());
		// String idl = ProtobufIDLGenerator.getIDL(AddressBookProtosPOJO.class);
		// System.out.println(idl);
	}
}
