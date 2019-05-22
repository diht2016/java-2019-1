import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BeanUtils {
    /**
     * Scans object "from" for all getters. If object "to"
     * contains correspondent setter, it will invoke it
     * to set property value for "to" which equals to the property
     * of "from".
     *
     * The type in setter should be compatible to the value returned
     * by getter (if not, no invocation performed).
     * Compatible means that parameter type in setter should
     * be the same or be superclass of the return type of the getter.
     *
     * The method takes care only about public methods.
     *
     * @param to   Object which properties will be set.
     * @param from Object which properties will be used to get values.
     *      
     */
    public static void assign(Object to, Object from) {
        Class<?> fromClass = from.getClass();
        Class<?> toClass = to.getClass();
        for (Method getter : fromClass.getMethods()) {
            String getterName = getter.getName();
            Class<?> returnedClass = getter.getReturnType();

            if (getter.getParameterCount() != 0
                    || !getterName.startsWith("get")
                    || returnedClass.equals(Void.TYPE)) {
                // method is not a getter
                continue;
            }

            String setterName = 's' + getterName.substring(1); // "getSomething" -> "setSomething"
            while (returnedClass != null) {
                try {
                    Method setter = toClass.getMethod(setterName, returnedClass);
                    setter.invoke(to, getter.invoke(from));
                    break;
                } catch (NoSuchMethodException
                        | IllegalAccessException
                        | InvocationTargetException e) {
                    returnedClass = returnedClass.getSuperclass();
                }
            }
        }

    }
}
