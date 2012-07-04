package es.com.tuv.trip2.hibernate.querybuilder.strategies;

import es.com.tuv.trip2.hibernate.querybuilder.QueryBuilder;

public interface QueryBuilderStrategy {

	/**
	 * Actualiza el queryBuilder aplicando filtros customizables.
	 * 
	 * @param <RT>
	 * @param queryBuilder - El queryBuilder original
	 * @param request - El mensaje recibido
	 * @param localQueryBuilder - Una instancia limpia del QueryBuilder para realizar consultas internamente
	 */
	public <RT> void updateQuery(QueryBuilder queryBuilder, RT request, QueryBuilder localQueryBuilder);

}
