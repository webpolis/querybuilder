package es.com.tuv.trip2.hibernate.querybuilder.helpers;

import java.util.List;

public interface QueryBuilderHelper {
	public <RT, E> List<E> runQuery(RT request);
}
