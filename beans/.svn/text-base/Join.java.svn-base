package es.com.tuv.trip2.hibernate.querybuilder.beans;

import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.PList;
import com.mysema.query.types.path.PSet;

@SuppressWarnings("rawtypes")
public class Join {

	protected PSet entitySet = null;
	protected PList entityList = null;
	protected PEntity entity = null;
	protected PEntity alias = null;
	protected boolean fetch = false;

	public <P> Join(PList entity, PEntity alias, boolean fetch) {
		this.entityList = entity;
		this.alias = alias;
		this.fetch = fetch;
	}

	public <P> Join(PSet<P> entity, PEntity alias, boolean fetch) {
		this.entitySet = entity;
		this.alias = alias;
		this.fetch = fetch;
	}

	public <P> Join(PEntity<P> entity, PEntity alias, boolean fetch) {
		this.entity = entity;
		this.alias = alias;
		this.fetch = fetch;
	}

	public <P> Join(PEntity<P> entity, boolean fetch) {
		this.entity = entity;
		this.fetch = fetch;
	}

	public <P> Join(PSet<P> entity, boolean fetch) {
		this.entitySet = entity;
		this.fetch = fetch;
	}

	public PList getEntityList() {
		return entityList;
	}

	public void setEntityList(PList entityList) {
		this.entityList = entityList;
	}

	public PSet getEntitySet() {
		return entitySet;
	}

	public void setEntitySet(PSet entity) {
		this.entitySet = entity;
	}

	public PEntity getAlias() {
		return alias;
	}

	public void setAlias(PEntity alias) {
		this.alias = alias;
	}

	public boolean isFetch() {
		return fetch;
	}

	public void setFetch(boolean fetch) {
		this.fetch = fetch;
	}

	public PEntity getEntity() {
		return entity;
	}

	public <PE> void setEntity(PEntity<PE> entity) {
		this.entity = entity;
	}

	public boolean equals(Object j) {
		if (j == null || !(j instanceof Join)) {
			return false;
		}
		boolean eq = (this.alias != null) ? ((Join) j).getAlias().equals(this.alias) : false;
		eq = eq && this.entity != null ? ((Join) j).getEntity().equals(this.entity) : eq;

		return eq;
	}

	public int hashCode() {
		int hash = (this.entity != null) ? this.entity.hashCode() : 0;
		hash += (this.alias != null) ? this.alias.hashCode() : 0;
		return hash;
	}
}
