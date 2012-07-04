package es.com.tuv.trip2.hibernate.querybuilder.helpers;

import java.util.List;
import es.com.tuv.trip2.hibernate.pojos.Expedientes;
import es.com.tuv.trip2.hibernate.pojos.QExpedientes;
import es.com.tuv.trip2.hibernate.querybuilder.enums.QueryBuilderSectionEnum;

public class ExpedientsQueryBuilderHelperImpl extends QueryBuilderHelperAbstract implements QueryBuilderHelper {

	@SuppressWarnings("unchecked")
	@Override
	public <RT, E> List<E> runQuery(RT request) {

		// cargar propiedades
		this.loadProperties(QueryBuilderSectionEnum.expedientes);

		// construir query
		this.buildQuery(request);
		
		List<Expedientes> exps = this.queryBuilder.selectAll(QExpedientes.expedientes);
		return (List<E>) exps;
	}
}
