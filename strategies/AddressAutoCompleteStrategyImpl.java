package es.com.tuv.trip2.hibernate.querybuilder.strategies;

import java.util.Arrays;

import es.com.tuv.trip2.hibernate.pojos.Personas;
import es.com.tuv.trip2.hibernate.pojos.QEquiposOrganizacionesRoles;
import es.com.tuv.trip2.hibernate.pojos.QOrganizaciones;
import es.com.tuv.trip2.hibernate.pojos.QPersonas;
import es.com.tuv.trip2.hibernate.pojos.QPersonasOrganizacionesRoles;
import es.com.tuv.trip2.hibernate.pojos.QUsuarios;
import es.com.tuv.trip2.hibernate.querybuilder.QueryBuilder;
import es.com.tuv.trip2.hibernate.ws.types.FindUserOrganizationRelatedAddressesRequest;

public class AddressAutoCompleteStrategyImpl implements QueryBuilderStrategy {

	/**
	 * nueva instancia del querybuilder para consultas locales
	 */
	private QueryBuilder localQueryBuilder = null;

	@Override
	public <RT> void updateQuery(QueryBuilder queryBuilder, RT request, QueryBuilder localQueryBuilder) {
		if (request != null && queryBuilder != null && localQueryBuilder != null) {
			FindUserOrganizationRelatedAddressesRequest req = (FindUserOrganizationRelatedAddressesRequest) request;
			this.localQueryBuilder = localQueryBuilder;

			QEquiposOrganizacionesRoles eor = QEquiposOrganizacionesRoles.equiposOrganizacionesRoles;
			QOrganizaciones org = QOrganizaciones.organizaciones;

			if (req.getUserID() != null) {
				// obtener persona del usuario que consulta
				QUsuarios users = QUsuarios.usuarios;
				QPersonas personas = QPersonas.personas;
				Personas person = this.localQueryBuilder.addFrom(personas).addJoin(personas.usuarios, users, false)
						.addWhere(users.idUsuario.equalsIgnoreCase(req.getUserID())).selectUnique(personas);

				if (person != null) {
					this.localQueryBuilder.clear();

					// obtener organizacion de pertenencia de la persona
					QPersonasOrganizacionesRoles por = QPersonasOrganizacionesRoles.personasOrganizacionesRoles;

					Object[] orgIdsPerson = this.localQueryBuilder
							.addFrom(org)
							.addJoin(org.personaOrganizacionRoleses, por, false)
							.addWhere(
									por.persona.idPersona.equalsIgnoreCase(person.getIdPersona()).and(
											por.rol.codigoEtiqueta.equalsIgnoreCase("etiq_rol_pertenencia")))
							.addOrder(org.idOrganizacion.asc()).selectAll(org.idOrganizacion).toArray();
					String[] personOrganizations = Arrays.copyOf(orgIdsPerson, orgIdsPerson.length, String[].class);

					this.localQueryBuilder.clear();

					// filtrar solo a equipos de su organizacion
					queryBuilder = queryBuilder.addWhere(eor.organizacion.idOrganizacion.in(personOrganizations));
				}

				this.localQueryBuilder.clear();
			}
		}
	}
}
