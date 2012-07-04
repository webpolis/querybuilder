package es.com.tuv.trip2.hibernate.querybuilder.strategies;

import es.com.tuv.trip2.hibernate.pojos.Equipos;
import es.com.tuv.trip2.hibernate.pojos.QEquipos;
import es.com.tuv.trip2.hibernate.pojos.QTablaColumna;
import es.com.tuv.trip2.hibernate.pojos.QTipoBusquedaActividadUbicacionTabla;
import es.com.tuv.trip2.hibernate.querybuilder.QueryBuilder;
import es.com.tuv.trip2.hibernate.ws.types.FindMenuByEquipmentRequest;

public class EquipmentMenuStrategyImpl implements QueryBuilderStrategy {

	/**
	 * nueva instancia del querybuilder para consultas locales
	 */
	private QueryBuilder localQueryBuilder = null;

	@Override
	public <RT> void updateQuery(QueryBuilder queryBuilder, RT request, QueryBuilder localQueryBuilder) {
		if (request != null && queryBuilder != null && localQueryBuilder != null) {
			FindMenuByEquipmentRequest req = (FindMenuByEquipmentRequest) request;
			this.localQueryBuilder = localQueryBuilder;

			// obtener actividad del equipo
			QEquipos eq = QEquipos.equipos;
			Equipos equipo = this.localQueryBuilder.addFrom(eq).addWhere(eq.idEquipo.equalsIgnoreCase(req.getFieldMenuByIdEquipment()))
					.addJoin(eq.listaActividades.actividadPadre, true).selectUnique(eq);
			this.localQueryBuilder.clear();

			String actIds[] = new String[] {
					equipo.getListaActividades().getIdActividad(),
					equipo.getListaActividades().getActividadPadre().getIdActividad() };
			
			// obtener tabla relacionada a la actividad en tbaut
			QTipoBusquedaActividadUbicacionTabla tbaut = QTipoBusquedaActividadUbicacionTabla.tipoBusquedaActividadUbicacionTabla;
			String idTabla = (String)this.localQueryBuilder.addFrom(tbaut).addWhere(tbaut.actividad.idActividad.in(actIds))
					.selectAll(tbaut.tabla.id).get(0);
			this.localQueryBuilder.clear();

			// agregar filtro a tablaColumna = tabla
			QTablaColumna tc = QTablaColumna.tablaColumna;
			queryBuilder = queryBuilder.addWhere(tc.tabla.id.eq(idTabla));
		}
	}
}
