package es.com.tuv.trip2.hibernate.querybuilder.helpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import es.com.tuv.trip2.hibernate.pojos.ColumnaMenu;
import es.com.tuv.trip2.hibernate.pojos.Menu;
import es.com.tuv.trip2.hibernate.pojos.QColumna;
import es.com.tuv.trip2.hibernate.pojos.QColumnaMenu;
import es.com.tuv.trip2.hibernate.querybuilder.enums.QueryBuilderSectionEnum;

public class EquipmentMenuQueryBuilderHelperImpl extends QueryBuilderHelperAbstract implements QueryBuilderHelper {

	@SuppressWarnings("unchecked")
	@Override
	public <RT, E> List<E> runQuery(RT request) {

		// cargar propiedades
		this.loadProperties(QueryBuilderSectionEnum.equipmentMenu);

		// construir query
		this.buildQuery(request);

		// obtener columnas de tipo menu para la tabla asociada a la actividad del equipo
		List<ColumnaMenu> cols = this.queryBuilder.selectAll(QColumna.columna.as(QColumnaMenu.class));

		List<Menu> menus = new ArrayList<Menu>();

		Iterator it = cols.iterator();
		while (it.hasNext()) {
			ColumnaMenu colmen = (ColumnaMenu) it.next();
			if (colmen.getMenu() != null) {
				menus.add(colmen.getMenu());
			}
		}

		return (List<E>) menus;
	}
}
