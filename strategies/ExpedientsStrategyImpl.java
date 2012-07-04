package es.com.tuv.trip2.hibernate.querybuilder.strategies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.com.tuv.trip2.hibernate.pojos.Personas;
import es.com.tuv.trip2.hibernate.pojos.QExpedientes;
import es.com.tuv.trip2.hibernate.pojos.QExpedientesOrganizacionesRoles;
import es.com.tuv.trip2.hibernate.pojos.QOrganizaciones;
import es.com.tuv.trip2.hibernate.pojos.QPersonas;
import es.com.tuv.trip2.hibernate.pojos.QPersonasOrganizacionesRoles;
import es.com.tuv.trip2.hibernate.pojos.QUsuarios;
import es.com.tuv.trip2.hibernate.querybuilder.QueryBuilder;
import es.com.tuv.trip2.hibernate.ws.types.FindExpedientsRequest;

public class ExpedientsStrategyImpl implements QueryBuilderStrategy {

	/**
	 * nueva instancia del querybuilder para consultas locales
	 */
	private QueryBuilder localQueryBuilder = null;

	@Override
	public <RT> void updateQuery(QueryBuilder queryBuilder, RT request, QueryBuilder localQueryBuilder) {
		if (request != null && queryBuilder != null && localQueryBuilder != null) {
			FindExpedientsRequest req = (FindExpedientsRequest) request;
			this.localQueryBuilder = localQueryBuilder;

			QExpedientesOrganizacionesRoles eor = QExpedientesOrganizacionesRoles.expedientesOrganizacionesRoles;
			QOrganizaciones org = QOrganizaciones.organizaciones;

			if (req.getUserId() != null) {
				// obtener persona del usuario que consulta
				QUsuarios users = QUsuarios.usuarios;
				QPersonas personas = QPersonas.personas;
				Personas person = this.localQueryBuilder.addFrom(personas).addJoin(personas.usuarios, users, false)
						.addWhere(users.idUsuario.equalsIgnoreCase(req.getUserId())).selectUnique(personas);

				if (person != null) {
					this.localQueryBuilder.clear();

					// obtener organizacion de pertenencia de la persona
					QPersonasOrganizacionesRoles por = QPersonasOrganizacionesRoles.personasOrganizacionesRoles;
					QExpedientes exp = QExpedientes.expedientes;

					Object[] orgIdsPerson = this.localQueryBuilder
							.addFrom(org)
							.addJoin(org.personaOrganizacionRoleses, por, false)
							.addWhere(
									por.persona.idPersona.equalsIgnoreCase(person.getIdPersona()).and(
											por.rol.codigoEtiqueta.equalsIgnoreCase("etiq_rol_pertenencia")))
							.addOrder(org.idOrganizacion.asc()).selectAll(org.idOrganizacion).toArray();
					String[] personOrganizations = Arrays.copyOf(orgIdsPerson, orgIdsPerson.length, String[].class);

					this.localQueryBuilder.clear();

					// obtener id de expedientes con algun rol para la organizacion del usuario
					List<String> personOrgExps = this.localQueryBuilder.addFrom(exp)
							.addJoin(exp.expedienteOrganizacionRoles, eor, false)
							.addWhere(eor.organizacion.idOrganizacion.in(personOrganizations)).selectAll(exp.idExpediente);

					queryBuilder = queryBuilder.addWhere(exp.idExpediente.in(personOrgExps));
				}

				this.localQueryBuilder.clear();
			}

			// obtener organizacion del filtro y sus hijas
			if (req.getOrganizationID() != null && req.getOrganizationID().size() > 0) {
				List<String> organizationIds = new ArrayList<String>();
				for (String id : req.getOrganizationID()) {
					if (id != null && id.trim().length() > 0) {
						organizationIds.add(id);
					}
				}

				if (organizationIds.size() > 0) {
					String[] organizacionesHijasStr = new String[] {};

					// obtener organizaciones hijas
					Object[] organizacionesHijas = this.localQueryBuilder.addFrom(org)
							.addWhere(org.organizacion.idOrganizacion.in(organizationIds)).selectAll(org.idOrganizacion)
							.toArray();

					if (organizacionesHijas.length > 0) {
						organizacionesHijasStr = Arrays.copyOf(organizacionesHijas, organizacionesHijas.length,
								String[].class);
						organizationIds.addAll(Arrays.asList(organizacionesHijasStr));
					}

					// agregar organizaciones al filtro
					queryBuilder = queryBuilder.addWhere(eor.organizacion.idOrganizacion.in(organizationIds).and(
							eor.rol.codigoEtiqueta.equalsIgnoreCase("etiq_rol_titular")));
				}
			}
		}
	}
}
