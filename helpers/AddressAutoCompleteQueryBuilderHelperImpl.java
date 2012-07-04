package es.com.tuv.trip2.hibernate.querybuilder.helpers;

import java.util.ArrayList;
import java.util.List;

import es.com.tuv.trip2.hibernate.pojos.Direcciones;
import es.com.tuv.trip2.hibernate.pojos.EquiposDireccionesTiposdirecciones;
import es.com.tuv.trip2.hibernate.pojos.QEquiposDireccionesTiposdirecciones;
import es.com.tuv.trip2.hibernate.querybuilder.enums.QueryBuilderSectionEnum;

public class AddressAutoCompleteQueryBuilderHelperImpl extends QueryBuilderHelperAbstract implements QueryBuilderHelper {

	@SuppressWarnings("unchecked")
	@Override
	public <RT, E> List<E> runQuery(RT request) {

		// cargar propiedades
		this.loadProperties(QueryBuilderSectionEnum.addressAutoComplete);

		// construir query
		this.buildQuery(request);

		List<EquiposDireccionesTiposdirecciones> edtds = this.queryBuilder
				.selectAll(QEquiposDireccionesTiposdirecciones.equiposDireccionesTiposdirecciones);

		List<Direcciones> addresses = new ArrayList<Direcciones>();
		if (edtds != null && edtds.size() > 0) {
			for (EquiposDireccionesTiposdirecciones edtd : edtds) {
				addresses.add(edtd.getDirecciones());
			}
		}

		return (List<E>) addresses;
	}
}
