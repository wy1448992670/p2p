package encoding;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class JavassistTest {
	public static void main(String[] args) throws NotFoundException {
		//javassist.CtClassType.getField
		
		ClassPool pool = ClassPool.getDefault();
		CtClass cc = pool.get("business.User");
		cc.getField("datetime");
		System.out.println("cc.getField(\"datetime\")");
	}
}
