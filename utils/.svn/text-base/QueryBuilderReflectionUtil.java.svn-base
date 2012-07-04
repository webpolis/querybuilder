package es.com.tuv.trip2.hibernate.querybuilder.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.PList;
import com.mysema.query.types.path.PSet;

public class QueryBuilderReflectionUtil {

	public final static String QUERYDSL_POJO_LOCATION = "es.com.tuv.trip2.hibernate.pojos";
	public final static String QUERYDSL_POJO_PREFIX = "Q";

	/**
	 * Obtenemos la instancia estatica de la entidad denominada por el parametro name
	 * 
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("rawtypes")
	public static PEntity getStaticEntityRefByName(String name) throws ClassNotFoundException, IllegalArgumentException,
			IllegalAccessException {
		Class aClass = Class.forName(name);
		Field[] fields = aClass.getFields();
		PEntity aliasEntity = null;

		// buscamos la variable que hace referencia a la entidad que unimos
		for (Field ff : fields) {
			if (ff.getType().equals(aClass)) {
				aliasEntity = (PEntity) ff.get(null);
				break;
			}
		}

		return aliasEntity;
	}

	/**
	 * Obtener el set de entidades o la entidad que se relacionan de acuerdo a la entidad padre y el nombre de la propiedad
	 * que define la relacion
	 * 
	 * @param mainEntity
	 * @param joinEntityPropName
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("rawtypes")
	public static Object getRelatedEntityByEntityAndPropName(PEntity mainEntity, String joinEntityPropName)
			throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field f = mainEntity.getClass().getField(joinEntityPropName);
		Object o = f.get(mainEntity);
		return o;
	}

	/**
	 * Obtenemos una referencia al campo definido en el string property. En caso de que existan varios niveles de relaciones,
	 * se itera hasta llegar al campo buscado. Obtiene el metodo segun su nombre y el campo al que pertenece. Finalmente se
	 * invoca el metodo utilizando los argumentos recibidos
	 * 
	 * 
	 * @param methodName
	 * @param aliasEntity
	 * @param property
	 * @param arguments
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("rawtypes")
	public static <T> Object invokeMethodByNameEntityPropAndArgument(String methodName, PEntity aliasEntity, String property,
			T arguments) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Object ret = null;
		Field f = null;
		PEntity middleEntity = null;
		PEntity thirdEntity = null;
		String tree[] = property.split("\\.");
		String filteredProperty = tree[tree.length - 1];

		// depende de la cantidad de niveles en las relaciones, instanciamos las entidades que falten
		// y obtenemos acceso al field que usamos para filtrar
		if (tree.length <= 2) {
			f = aliasEntity.getClass().getField(filteredProperty);
		} else if (tree.length == 3) {
			middleEntity = (PEntity) getRelatedEntityByEntityAndPropName(aliasEntity, tree[1]);
			f = middleEntity.getClass().getField(filteredProperty);
		} else if (tree.length == 4) {
			middleEntity = (PEntity) getRelatedEntityByEntityAndPropName(aliasEntity, tree[1]);
			try {
				thirdEntity = (PEntity) getStaticEntityRefByName(middleEntity.getClass().getField(tree[2]).getType()
						.getName());
				f = thirdEntity.getClass().getField(filteredProperty);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		// ubicar metodo requerido
		Method m = null;

		Method methods[] = f.getType().getMethods();
		for (Method mm : methods) {
			if (mm.getName().equalsIgnoreCase(methodName)) {
				if (mm.getReturnType().equals(EBoolean.class) || mm.getReturnType().equals(OrderSpecifier.class)) {
					Class params[] = mm.getParameterTypes();

					// ignorar ciertos signatures para el metodo CONTAINS
					if (methodName.equalsIgnoreCase("contains")) {
						if (params.length > 1 || !params[0].equals(String.class)) {
							continue;
						}
					}

					// ignorar ciertos signatures para el metodo IN
					if (methodName.equalsIgnoreCase("in") && !params[0].equals(Object[].class)) {
						continue;
					}

					// ignorar ciertos signatures para el metodo EQUALSIGNORECASE
					if (methodName.equalsIgnoreCase("equalsignorecase") && !params[0].equals(String.class)) {
						continue;
					}

					if (m == null) {
						m = mm;
						break;
					}
				}
			}
		}

		// invocar metodo pasando los argumentos recibidos
		if (arguments != null) {
			PEntity propOwnerEntity = null;

			if (tree.length == 3) {
				propOwnerEntity = middleEntity;
			} else if (tree.length >= 4) {
				propOwnerEntity = thirdEntity;
			} else {
				propOwnerEntity = aliasEntity;
			}

			boolean booleanValue = (((String) arguments).matches("true|false")) ? true : false;
			ret = m.invoke(f.get(propOwnerEntity), (booleanValue ? Boolean.valueOf((String) arguments) : arguments));
		} else {
			ret = m.invoke(f.get(((tree.length > 2) ? middleEntity : aliasEntity)));
		}

		return ret;
	}

	@SuppressWarnings("rawtypes")
	public static <T> PEntity getAliasByEntity(T relatedObj) throws IllegalArgumentException, ClassNotFoundException,
			IllegalAccessException {
		PEntity aliasEntity = null;
		String alias = null;

		if (relatedObj.getClass().equals(PSet.class)) {
			PSet jEnt = (PSet) relatedObj;
			alias = QUERYDSL_POJO_LOCATION + "." + QUERYDSL_POJO_PREFIX + jEnt.getEntityName();
			aliasEntity = QueryBuilderReflectionUtil.getStaticEntityRefByName(alias);
		} else if (relatedObj.getClass().equals(PList.class)) {
			PList jEnt = (PList) relatedObj;
			alias = QUERYDSL_POJO_LOCATION + "." + jEnt.get(0).getClass().getSimpleName();
			aliasEntity = QueryBuilderReflectionUtil.getStaticEntityRefByName(alias);
		} else {
			PEntity jEnt = (PEntity) relatedObj;
			alias = QUERYDSL_POJO_LOCATION + "." + jEnt.getClass().getSimpleName();
			aliasEntity = QueryBuilderReflectionUtil.getStaticEntityRefByName(alias);
		}
		
		return aliasEntity;
	}

}
