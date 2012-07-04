package es.com.tuv.trip2.hibernate.querybuilder.helpers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.mysema.query.types.Expr;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.PList;
import com.mysema.query.types.path.PSet;

import es.com.tuv.trip2.hibernate.querybuilder.QueryBuilder;
import es.com.tuv.trip2.hibernate.querybuilder.beans.Filter;
import es.com.tuv.trip2.hibernate.querybuilder.beans.Order;
import es.com.tuv.trip2.hibernate.querybuilder.enums.QueryBuilderSectionEnum;
import es.com.tuv.trip2.hibernate.querybuilder.utils.QueryBuilderReflectionUtil;

public abstract class QueryBuilderHelperAbstract {

	private final static String PROPERTY_FILE_NAME = "querybuilder.properties";
	private final static String PROPERTY_JOIN_STR = "join";
	private final static String PROPERTY_FROM_STR = "from";
	private final static String PROPERTY_ORDER_STR = "order";
	private final static String PROPERTY_GROUP_STR = "group";
	private final static String PROPERTY_FILTER_STR = "filter";
	private final static String PROPERTY_IMPL_STR = "impl";
	private final static String PROPERTY_EXPL_STR = "expl";
	private final static String PROPERTY_STRATEGY_STR = "strategies";
	private final static String PROPERTY_COUNT_STR = "count";
	private final static String PROPERTY_DEFAULT_STRING_STR = "DEFAULT_STRING_VALUE";
	private final static String PROPERTY_DEFAULT_NUMERIC_STR = "DEFAULT_NUMERIC_VALUE";

	private QueryBuilderSectionEnum section = null;

	protected QueryBuilder queryBuilder = null;
	protected QueryBuilder strategyLocalQueryBuilder = null;

	protected List<String> froms = null;
	protected List<Order> orders = null;
	protected LinkedHashMap<String, Boolean> joins = null;
	protected List<Filter> filtersImpl = null;
	protected HashMap<String, Filter> filtersExpl = null;
	protected List<String> strategies = null;
	protected List<String> groups = null;

	public QueryBuilderHelperAbstract() {
		this.froms = new ArrayList<String>();
		this.joins = new LinkedHashMap<String, Boolean>();
		this.filtersExpl = new HashMap<String, Filter>();
		this.filtersImpl = new ArrayList<Filter>();
		this.strategies = new ArrayList<String>();
		this.orders = new ArrayList<Order>();
		this.groups = new ArrayList<String>();
	}

	/**
	 * Carga los listados definidos en la seccion "section" del archivo de propiedades
	 * 
	 * @param section
	 */
	public final void loadProperties(QueryBuilderSectionEnum section) {
		this.section = section;
		this.froms.clear();
		this.joins.clear();
		this.filtersExpl.clear();
		this.filtersImpl.clear();
		this.strategies.clear();
		this.orders.clear();

		try {
			Properties props = new Properties();
			InputStream is = this.getClass().getResourceAsStream("/" + PROPERTY_FILE_NAME);
			props.load(is);

			// cargar froms
			this.loadFroms(props);

			// cargar joins
			this.loadJoins(props);

			// cargar filtros implicitos
			this.loadFilterImpl(props);

			// cargar filtros explicitos
			this.loadFilterExpl(props);

			// cargar ordenamiento
			this.loadOrders(props);

			// cargar agrupamiento
			this.loadGroups(props);

			// cargar strategies
			this.loadStrategies(props);

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Inicializa el QueryBuilder y obtiene los datos necesarios del mensaje (request)
	 * 
	 * @param <RT>
	 * @param <T>
	 * @param request
	 */
	@SuppressWarnings({
			"rawtypes",
			"unchecked" })
	public final <RT, T> void buildQuery(RT request) {
		this.queryBuilder = this.queryBuilder.clear();

		// comenzar a construir el query
		if (this.froms.size() > 0) {
			try {
				// entidad primaria
				PEntity mainEntity = null;
				// cargar froms
				for (String from : this.froms) {
					String className = QueryBuilderReflectionUtil.QUERYDSL_POJO_LOCATION + "." + from.split("\\.")[0];
					PEntity ent = QueryBuilderReflectionUtil.getStaticEntityRefByName(className);
					if (mainEntity == null) {
						mainEntity = ent;
					}
					this.queryBuilder = this.queryBuilder.addFrom(ent);
				}

				// cargar ordenamiento
				if (this.orders.size() > 0) {
					Iterator it = this.orders.iterator();
					while (it.hasNext()) {
						Order o = (Order) it.next();
						OrderSpecifier orderMethod = (OrderSpecifier) QueryBuilderReflectionUtil
								.invokeMethodByNameEntityPropAndArgument(o.getType(), mainEntity, o.getField(), null);
						if (orderMethod != null) {
							this.queryBuilder = this.queryBuilder.addOrder(orderMethod);
						}
					}
				}

				// cargar filtros
				if (this.filtersExpl.size() > 0) {
					Iterator it = this.filtersExpl.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry vals = (Map.Entry) it.next();
						Filter f = (Filter) vals.getValue();
						if (f != null && !f.getEntity().contains(".")) {
							this.processFilter(f, request, mainEntity);
							it.remove();
						}
					}
				}

				if (this.filtersImpl.size() > 0) {
					Iterator it = this.filtersImpl.iterator();
					while (it.hasNext()) {
						Filter f = (Filter) it.next();
						if (f != null && !f.getEntity().contains(".")) {
							this.processFilter(f, request, mainEntity);
							it.remove();
						}
					}
				}

				// cargar joins y sus filtros correspondientes si los hay
				if (this.joins.size() > 0) {
					Iterator it = this.joins.entrySet().iterator();
					while (it.hasNext()) {
						// el alias permite tener una instancia auxiliar de una entidad para poder utilizar sus propiedades en
						// los filtros
						PEntity aliasEntity = null;
						PEntity joinedEntity = null;

						Map.Entry vals = (Map.Entry) it.next();
						String joinEntityPropName = (String) vals.getKey();

						// chequear cantidad de niveles de relacion
						String related[] = joinEntityPropName.split("\\.");
						int level = 0;
						for (String entity : related) {

							// si hay mas de una relacion, actualizar la entidad padre con la ultima entidad conocida
							if (level >= 1) {
								joinedEntity = aliasEntity;
							} else {
								joinedEntity = mainEntity;
							}

							Object relatedObj = QueryBuilderReflectionUtil.getRelatedEntityByEntityAndPropName(joinedEntity,
									entity);
							aliasEntity = QueryBuilderReflectionUtil.getAliasByEntity(relatedObj);

							// castear apropiadamente el tipo de relacion y cargar alias para futura referencia en filtros
							if (relatedObj.getClass().equals(PSet.class)) {
								this.queryBuilder = this.queryBuilder.addJoin((PSet) relatedObj, aliasEntity,
										(Boolean) vals.getValue());
							} else if (relatedObj.getClass().equals(PList.class)) {
								this.queryBuilder = this.queryBuilder.addJoin((PList) relatedObj, aliasEntity,
										(Boolean) vals.getValue());
							} else {
								this.queryBuilder = this.queryBuilder.addJoin((PEntity) relatedObj, aliasEntity,
										(Boolean) vals.getValue());
							}

							if (aliasEntity != null) {
								// busco el property name entre los filtros cargados, obtengo el filtro correspondiente
								Filter joinFilter = this.searchAndDequeueFilterByRelationName(entity);
								// procesa el filtro y agrega al querybuilder
								this.processFilter(joinFilter, request, aliasEntity);
							}
							level++;
						}
					}
				}

				// cargar groups
				if (this.groups.size() > 0) {
					Iterator it = this.groups.iterator();
					while (it.hasNext()) {
						PEntity aliasEntity = null;
						PEntity joinedEntity = null;

						String joinEntityPropName = (String) it.next();

						// chequear cantidad de niveles de relacion
						String related[] = joinEntityPropName.split("\\.");
						int level = 0;
						for (String entity : related) {

							// si hay mas de una relacion, actualizar la entidad padre con la ultima entidad conocida
							if (level >= 1) {
								joinedEntity = aliasEntity;
							} else {
								joinedEntity = mainEntity;
							}

							Object relatedObj = QueryBuilderReflectionUtil.getRelatedEntityByEntityAndPropName(joinedEntity,
									entity);
							if(level == (related.length-1)){
								this.queryBuilder = this.queryBuilder.addGroup((Expr)relatedObj);
							}else{
								aliasEntity = QueryBuilderReflectionUtil.getAliasByEntity(relatedObj);
							}
							
							level++;
						}
					}
				}

				// cargar strategies
				if (this.strategies.size() > 0) {
					Iterator it = this.strategies.iterator();

					while (it.hasNext()) {
						Class strategy = Class.forName((String) it.next());

						if (strategy != null) {
							this.strategyLocalQueryBuilder.clear();
							Method mm = strategy.getMethod("updateQuery", QueryBuilder.class, Object.class,
									QueryBuilder.class);
							mm.invoke(strategy.newInstance(), this.queryBuilder, Object.class.cast(request),
									this.strategyLocalQueryBuilder);
						}
					}
				}

			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void loadFroms(Properties props) throws NoSuchFieldException {
		String prop = props.getProperty(section.name() + "." + PROPERTY_FROM_STR + "." + PROPERTY_COUNT_STR);

		if (prop != null) {
			int fromCount = Integer.valueOf(prop);
			if (fromCount > 0) {
				for (int i = 0; i < fromCount; i++) {
					this.froms.add(props.getProperty(section.name() + "." + PROPERTY_FROM_STR + "." + String.valueOf(i)));
				}
			}
		}
	}

	private void loadJoins(Properties props) throws NoSuchFieldException {
		String prop = props.getProperty(section.name() + "." + PROPERTY_JOIN_STR + "." + PROPERTY_COUNT_STR);

		if (prop != null) {
			int joinCount = Integer.valueOf(prop);
			if (joinCount > 0) {
				for (int i = 0; i < joinCount; i++) {
					String l = props.getProperty(section.name() + "." + PROPERTY_JOIN_STR + "." + String.valueOf(i));
					String jEntity = String.valueOf(l.split(",")[0].trim());
					Boolean jFetch = Boolean.valueOf(l.split(",")[1].trim());
					this.joins.put(jEntity, jFetch);
				}
			}
		}
	}

	private void loadFilterImpl(Properties props) throws NoSuchFieldException {
		String prop = props.getProperty(section.name() + "." + PROPERTY_FILTER_STR + "." + PROPERTY_IMPL_STR + "."
				+ PROPERTY_COUNT_STR);

		if (prop != null) {
			int filterImplCount = Integer.valueOf(prop);
			if (filterImplCount > 0) {
				for (int i = 0; i < filterImplCount; i++) {
					String l = props.getProperty(section.name() + "." + PROPERTY_FILTER_STR + "." + PROPERTY_IMPL_STR + "."
							+ String.valueOf(i));
					if (l != null) {
						String entityProp = String.valueOf(l.split(",")[0].trim());
						String filterMethod = String.valueOf(l.split(",")[1].trim());
						String fixedValues[] = String.valueOf(l.split(",")[2].trim()).split(";");
						// inicializar filtro
						Filter filter = new Filter();
						filter.setDefaultImplicitValues(fixedValues);
						filter.setEntity(entityProp);
						filter.setMethod(filterMethod);
						this.filtersImpl.add(filter);
					}
				}
			}
		}
	}

	private void loadFilterExpl(Properties props) throws NoSuchFieldException {
		Set<Object> keys = props.keySet();
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			String p = String.valueOf(it.next());
			if (p.contains(section.name() + "." + PROPERTY_FILTER_STR + "." + PROPERTY_EXPL_STR + ".")) {
				// extraer nombres de parametros
				String param = p.replaceAll("^.*\\.([^\\.]+)$", "$1").trim();
				if (param != null && param.length() > 0) {
					String l = props.getProperty(section.name() + "." + PROPERTY_FILTER_STR + "." + PROPERTY_EXPL_STR + "."
							+ param);
					if (l != null) {
						// extraer valores del filtro
						String entityProp = String.valueOf(l.split(",")[0].trim());
						String filterMethod = String.valueOf(l.split(",")[1].trim());
						String defaultValue = String.valueOf(l.split(",")[2].trim());

						Filter filter = new Filter();
						filter.setExplicitFilter(true);
						filter.setEntity(entityProp);
						filter.setMethod(filterMethod);
						filter.setMessageParamName(param);

						if (defaultValue != null
								&& defaultValue.matches(PROPERTY_DEFAULT_STRING_STR + "|" + PROPERTY_DEFAULT_NUMERIC_STR)) {
							// obtener x reflection el tipo de default value e inicializar el filtro
							try {
								Field f = Filter.class.getField(defaultValue);
								if (f.getType().equals(String.class)) {
									filter.setDefaultStringValue(Filter.DEFAULT_STRING_VALUE);
								}
								if (f.getType().equals(int.class)) {
									filter.setDefaultNumericValue(Filter.DEFAULT_NUMERIC_VALUE);
								}
							} catch (NoSuchFieldError e) {
								e.printStackTrace();
							}
						} else if (defaultValue != null) {
							// o setear valor default customizado
							if (defaultValue.matches("^\\d+$")) {
								filter.setDefaultNumericValue(Integer.valueOf(defaultValue));
							} else {
								filter.setDefaultStringValue(defaultValue);
							}
						}
						this.filtersExpl.put(param, filter);
					}
				}
			}
		}
	}

	private void loadStrategies(Properties props) {
		String prop = props.getProperty(section.name() + "." + PROPERTY_STRATEGY_STR + "." + PROPERTY_COUNT_STR);

		if (prop != null) {
			int stratCount = Integer.valueOf(prop);
			if (stratCount > 0) {
				for (int i = 0; i < stratCount; i++) {
					this.strategies.add(props.getProperty(section.name() + "." + PROPERTY_STRATEGY_STR + "."
							+ String.valueOf(i)));
				}
			}
		}
	}

	private void loadOrders(Properties props) throws NoSuchFieldException {
		String prop = props.getProperty(section.name() + "." + PROPERTY_ORDER_STR + "." + PROPERTY_COUNT_STR);

		if (prop != null) {
			int orderCount = Integer.valueOf(prop);
			if (orderCount > 0) {
				for (int i = 0; i < orderCount; i++) {
					String value = props.getProperty(section.name() + "." + PROPERTY_ORDER_STR + "." + String.valueOf(i));
					Order o = new Order();
					o.setField(value.split(",")[0].trim());
					o.setType((value.split(",")[1].trim().equalsIgnoreCase(Order.ASC) ? Order.ASC : Order.DESC));
					this.orders.add(o);
				}
			}
		}
	}

	private void loadGroups(Properties props) throws NoSuchFieldException {
		String prop = props.getProperty(section.name() + "." + PROPERTY_GROUP_STR + "." + PROPERTY_COUNT_STR);

		if (prop != null) {
			int groupCount = Integer.valueOf(prop);
			if (groupCount > 0) {
				for (int i = 0; i < groupCount; i++) {
					this.groups.add(props.getProperty(section.name() + "." + PROPERTY_GROUP_STR + "." + String.valueOf(i)));
				}
			}
		}
	}

	/**
	 * Itera por los filtros cargados hasta encontrar aquel que corresponda a la entidad relacionada definida por "relName"
	 * Este metodo remueve el elemento obtenido del listado interno, para evitar filtros repetidos
	 * 
	 * @param propName
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected final Filter searchAndDequeueFilterByRelationName(String relName) {
		// iterar por filtros implicitos
		if (this.filtersImpl.size() > 0) {
			Iterator it = this.filtersImpl.iterator();
			while (it.hasNext()) {
				Filter f = (Filter) it.next();
				if (f != null && f.getEntity().contains(".") && f.getEntity().split("\\.")[0].equalsIgnoreCase(relName)) {
					it.remove();
					return f;
				}
			}
		}

		// iterar por filtros explicitos
		if (this.filtersExpl.size() > 0) {
			Iterator it = this.filtersExpl.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry vals = (Map.Entry) it.next();
				Filter f = (Filter) vals.getValue();
				if (f != null && f.getEntity().contains(".") && f.getEntity().split("\\.")[0].equalsIgnoreCase(relName)) {
					it.remove();
					return f;
				}
			}
		}

		return null;
	}

	/**
	 * Procesa un filtro invocando los metodos habilitados en las propiedades de la entidad, obteniendo los valores del
	 * request en caso de ser filtro explicito, y actualiza el querybuilder
	 * 
	 * @param <RT>
	 * @param <T>
	 * @param joinFilter
	 * @param request
	 * @param aliasEntity
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings({
			"unchecked",
			"rawtypes" })
	private <RT, T> void processFilter(Filter joinFilter, RT request, PEntity aliasEntity) throws SecurityException,
			IllegalArgumentException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
		if (joinFilter != null && joinFilter.getEntity() != null) {
			T argument = null;
			// obtener argumentos para el filtro
			if (joinFilter.isExplicitFilter()) {
				// si es explicito buscamos el valor enviado a traves del request
				// para eso iteramos por los metodos hasta dar con el getter adecuado
				Method methods[] = request.getClass().getMethods();
				for (Method m : methods) {
					if (m.getName().equalsIgnoreCase("get" + joinFilter.getMessageParamName())) {
						argument = (T) m.getReturnType().cast(m.invoke(request));
						break;
					}
				}
			} else {
				// para filtros implicitos, usamos los valores ya definidos
				String defValues[] = joinFilter.getDefaultImplicitValues();
				argument = defValues.length == 1 ? (T) defValues[0] : (T) defValues;
			}

			// si hay valores vacios, no agregar filtro
			if (argument != null && !this.hasArgumentEmptyValue(argument, joinFilter)) {
				EBoolean filterRet = (EBoolean) QueryBuilderReflectionUtil.invokeMethodByNameEntityPropAndArgument(
						joinFilter.getMethod(), aliasEntity, joinFilter.getEntity(), argument);

				this.queryBuilder = this.queryBuilder.addWhere(filterRet);
			}
		}
	}

	/**
	 * Metodo de ayuda para determinar si el valor del argumento coincide con los valores por default que definen a un campo
	 * como ignorado o vacio
	 * 
	 * @param <T>
	 * @param argument
	 * @param joinFilter
	 * @return
	 */
	private <T> boolean hasArgumentEmptyValue(T argument, Filter joinFilter) {
		boolean ignore = false;
		boolean isNum = argument.getClass().equals(BigInteger.class);
		boolean isStr = argument.getClass().equals(String.class);
		if (isNum || isStr) {
			BigInteger emptyNum = BigInteger.valueOf(joinFilter.getDefaultNumericValue());
			String emptyStr = joinFilter.getDefaultStringValue();
			ignore = (isNum && emptyNum.equals(argument));
			ignore = !ignore ? (isStr && emptyStr.equals(argument)) : ignore;
		}

		return ignore;
	}

	public QueryBuilder getQueryBuilder() {
		return queryBuilder;
	}

	public void setQueryBuilder(QueryBuilder queryBuilder) {
		this.queryBuilder = queryBuilder;
	}

	public List<String> getFroms() {
		return froms;
	}

	public HashMap<String, Boolean> getJoins() {
		return joins;
	}

	public List<String> getStrategies() {
		return strategies;
	}

	public List<Filter> getFiltersImpl() {
		return filtersImpl;
	}

	public HashMap<String, Filter> getFiltersExpl() {
		return filtersExpl;
	}

	public QueryBuilder getStrategyLocalQueryBuilder() {
		return strategyLocalQueryBuilder;
	}

	public void setStrategyLocalQueryBuilder(QueryBuilder strategyLocalQueryBuilder) {
		this.strategyLocalQueryBuilder = strategyLocalQueryBuilder;
	}

}
