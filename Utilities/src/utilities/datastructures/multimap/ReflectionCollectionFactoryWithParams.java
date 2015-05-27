package utilities.datastructures.multimap;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.FunctorException;

/**
 * TODO: document
 * 
 * @param <V>
 *            the concrete type of the values in the {@link Collection}
 * @param <R>
 *            the concrete type of {@link Collection} to instantiate
 * @param <T>
 *            the type of {@link Collection} to instantiate without parameter types
 */
public class ReflectionCollectionFactoryWithParams<V, R extends Collection<V>, T extends Collection<?>> implements Factory<R>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2692743986733533160L;

	/**
	 * The class of the {@link Collection} to instantiate.
	 */
	private final Class<T> collectionClass;

	/**
	 * The parameters given to call the constructor.
	 */
	private final Object[] constructorParams;

	/**
	 * The class types of the parameters to identify the right constructor.
	 */
	private final Class<?>[] constructorParamsClasses;

	/**
	 * Holds wrapper object classes to primitive classes for reflection purposes as primitive classes are not found by
	 * reflection when calling getConstructor.
	 */
	private static Map<Class<?>, Class<?>> objectToPrim = new HashMap<Class<?>, Class<?>>();

	static {
		ReflectionCollectionFactoryWithParams.objectToPrim.put(Integer.class, int.class);
		ReflectionCollectionFactoryWithParams.objectToPrim.put(Long.class, long.class);
		ReflectionCollectionFactoryWithParams.objectToPrim.put(Double.class, double.class);
		ReflectionCollectionFactoryWithParams.objectToPrim.put(Float.class, float.class);
		ReflectionCollectionFactoryWithParams.objectToPrim.put(Boolean.class, boolean.class);
		ReflectionCollectionFactoryWithParams.objectToPrim.put(Byte.class, byte.class);
		ReflectionCollectionFactoryWithParams.objectToPrim.put(Character.class, char.class);
	}

	/**
	 * Creates a new {@link ReflectionCollectionFactoryWithParams} in order to instantiate a {@link Collection} as
	 * needed.
	 * 
	 * @param clazz
	 *            the class of the concrete type of collection to instantiate
	 * @param params
	 *            the constructor parameters to give when instantiating the object
	 */
	public ReflectionCollectionFactoryWithParams(Class<T> clazz, Object... params) {
		// TODO: handle NullObject<T>
		this.collectionClass = clazz;
		this.constructorParams = params;
		if (params != null && params.length > 0) {
			this.constructorParamsClasses = new Class<?>[params.length];
			int i = 0;
			for (Object param : params) {
				// as primitive values are not found by reflection when calling getConstructor with the wrapper object
				// class, get the primitive class
				if (ReflectionCollectionFactoryWithParams.objectToPrim.containsKey(param.getClass()))
					this.constructorParamsClasses[i++] = ReflectionCollectionFactoryWithParams.objectToPrim.get(param.getClass());
				else
					this.constructorParamsClasses[i++] = param.getClass();
			}
		} else {
			this.constructorParamsClasses = new Class<?>[0];
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public R create() {
		try {
			Constructor<T> c = this.collectionClass.getConstructor(this.constructorParamsClasses);
			T newInstance = c.newInstance(this.constructorParams);
			return (R) newInstance;
		} catch (InstantiationException e) {
			throw new FunctorException("Unable to instantiate class '" + this.collectionClass + "'!");
		} catch (IllegalAccessException e) {
			throw new FunctorException("Illegal access to class '" + this.collectionClass + "'!");
		} catch (NoSuchMethodException e) {
			throw new FunctorException("No such method in class '" + this.collectionClass + "'!");
		} catch (SecurityException e) {
			throw new FunctorException("Security Exception in class '" + this.collectionClass + "'!");
		} catch (IllegalArgumentException e) {
			throw new FunctorException("Unable to instantiate class '" + this.collectionClass + "'!");
		} catch (InvocationTargetException e) {
			throw new FunctorException("Unable to instantiate class '" + this.collectionClass + "'!");
		}
	}
}