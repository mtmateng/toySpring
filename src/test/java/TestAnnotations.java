import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;

public class TestAnnotations {

    public static void main(String[] args) {

        for (Method declaredMethod : TestAnnotations.class.getDeclaredMethods()) {
            if (void.class.isAssignableFrom(declaredMethod.getReturnType())) {
                continue;
            }
            System.out.println(((ParameterizedType)declaredMethod.getGenericReturnType()).getActualTypeArguments()[0]);
//            for (Parameter parameter : declaredMethod.getParameters()) {
//                System.out.println(parameter.getParameterizedType().getClass());
//            }
        }

    }

    public List<String> fuckList(List<String> param1) {
        return null;
    }
}
