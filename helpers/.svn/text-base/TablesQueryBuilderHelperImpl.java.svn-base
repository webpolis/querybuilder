package es.com.tuv.trip2.hibernate.querybuilder.helpers;

import java.util.ArrayList;
import java.util.List;
import es.com.tuv.trip2.hibernate.pojos.QTipoBusquedaActividadUbicacionTabla;
import es.com.tuv.trip2.hibernate.pojos.Tabla;
import es.com.tuv.trip2.hibernate.pojos.TipoBusquedaActividadUbicacionTabla;
import es.com.tuv.trip2.hibernate.querybuilder.enums.QueryBuilderSectionEnum;

public class TablesQueryBuilderHelperImpl extends QueryBuilderHelperAbstract implements QueryBuilderHelper {

	@SuppressWarnings("unchecked")
	@Override
	public <RT, E> List<E> runQuery(RT request) {

		// cargar propiedades
		this.loadProperties(QueryBuilderSectionEnum.tables);

		// construir query
		this.buildQuery(request);

		List<TipoBusquedaActividadUbicacionTabla> tbauts = this.queryBuilder
				.selectAll(QTipoBusquedaActividadUbicacionTabla.tipoBusquedaActividadUbicacionTabla);
		List<Tabla> tablas = new ArrayList<Tabla>();

		for (TipoBusquedaActividadUbicacionTabla tbaut : tbauts) {
			if (tbaut.getTabla() != null) {
				tablas.add(tbaut.getTabla());
			}
		}
		return (List<E>) tablas;
	}
}
