package es.com.tuv.trip2.hibernate.querybuilder;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;

import com.mysema.query.hql.HQLQuery;
import com.mysema.query.hql.hibernate.HibernateQuery;
import com.mysema.query.types.Expr;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.PList;
import com.mysema.query.types.path.PSet;

import es.com.tuv.trip2.hibernate.querybuilder.beans.Join;

@SuppressWarnings("rawtypes")
public class QueryBuilderImpl implements QueryBuilder {

	protected SessionFactory sessionFactory = null;

	private List<PEntity> from = null;
	private List<Join> join = null;
	private List<EBoolean> where = null;
	private List<OrderSpecifier> order = null;
	private List<Expr> group = null;
	private int limit = 0;
	private int offset = 0;

	public QueryBuilderImpl() {
		this.from = new ArrayList<PEntity>();
		this.where = new ArrayList<EBoolean>();
		this.order = new ArrayList<OrderSpecifier>();
		this.join = new ArrayList<Join>();
		this.group = new ArrayList<Expr>();
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public QueryBuilder addFrom(PEntity from) {
		if (from != null) {
			this.from.add(from);
		}
		return this;
	}

	@Override
	public QueryBuilder addFrom(PEntity[] from) {
		if (from != null && from.length > 0) {
			for (PEntity e : from) {
				this.from.add(e);
			}
		}
		return this;
	}

	@Override
	public <P> QueryBuilder addJoin(PSet<P> join, boolean fetch) {
		if (join != null) {
			Join j = new Join(join, fetch);
			if (!this.join.contains(j)) {
				this.join.add(j);
			}
		}
		return this;
	}

	@Override
	public <P> QueryBuilder addJoin(PSet<P> join, PEntity alias, boolean fetch) {
		if (join != null && alias != null) {
			Join j = new Join(join, alias, fetch);
			if (!this.join.contains(j)) {
				this.join.add(j);
			}
		}
		return this;
	}

	@Override
	public <P> QueryBuilder addJoin(PList<?, ?> join, PEntity alias, boolean fetch) {
		if (join != null && alias != null) {
			Join j = new Join(join, alias, fetch);
			if (!this.join.contains(j)) {
				this.join.add(j);
			}
		}
		return this;
	}

	@Override
	public <P> QueryBuilder addJoin(PEntity<P> join, boolean fetch) {
		if (join != null) {
			Join j = new Join(join, fetch);
			if (!this.join.contains(j)) {
				this.join.add(j);
			}
		}
		return this;
	}

	@Override
	public <P> QueryBuilder addJoin(PEntity<P> join, PEntity alias, boolean fetch) {
		if (join != null && alias != null) {
			Join j = new Join(join, alias, fetch);
			if (!this.join.contains(j)) {
				this.join.add(j);
			}
		}
		return this;
	}

	@Override
	public QueryBuilder addWhere(EBoolean where) {
		if (where != null) {
			this.where.add(where);
		}
		return this;
	}

	@Override
	public QueryBuilder addWhere(EBoolean[] where) {
		if (where != null && where.length > 0) {
			for (EBoolean b : where) {
				this.where.add(b);
			}
		}
		return this;
	}

	@Override
	public QueryBuilder addOrder(OrderSpecifier order) {
		if (order != null) {
			this.order.add(order);
		}
		return this;
	}

	@Override
	public QueryBuilder addOrder(OrderSpecifier[] order) {
		if (order != null && order.length > 0) {
			for (OrderSpecifier o : order) {
				this.order.add(o);
			}
		}
		return this;
	}

	@Override
	public List<Object[]> selectAll(Expr... column) {
		return this.makeSelect(column);
	}

	@Override
	public <RT> List<RT> selectAll(Expr column) {
		return this.makeSelect(column);
	}

	@Override
	public Object[] selectUnique(Expr... column) {
		return this.makeUniqueMultiColumnSelect(column);
	}

	@Override
	public <RT> RT selectUnique(Expr column) {
		return this.makeUniqueSelect(column);
	}

	@SuppressWarnings("unchecked")
	protected <RT> List<RT> makeSelect(Expr... column) {
		HQLQuery q = this.buildQuery();

		if (q != null) {
			return (List<RT>) q.listDistinct(column);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected <RT> RT makeUniqueSelect(Expr column) {
		HQLQuery q = this.buildQuery();

		if (q != null) {
			return (RT) q.uniqueResult(column);
		}
		return null;
	}

	protected Object[] makeUniqueMultiColumnSelect(Expr... column) {
		HQLQuery q = this.buildQuery();

		if (q != null) {
			return q.uniqueResult(column);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <PE> HQLQuery buildQuery() {
		HQLQuery q = new HibernateQuery(this.getSessionFactory().getCurrentSession());

		if (this.from != null && this.from.size() > 0) {
			for (PEntity f : this.from) {
				q = q.from(f);
			}
		}
		if (this.join != null && this.join.size() > 0) {
			for (Join j : this.join) {
				if (j.getEntityList() != null && j.getAlias() != null) {
					q = q.leftJoin(j.getEntityList(), (PEntity<Object>) j.getAlias());
				} else if (j.getEntitySet() != null && j.getAlias() != null) {
					q = q.leftJoin(j.getEntitySet(), (PEntity<Object>) j.getAlias());
				} else if (j.getEntitySet() != null && j.getAlias() == null) {
					q = q.leftJoin(j.getEntitySet());
				} else if (j.getEntity() != null && j.getAlias() != null) {
					q = q.leftJoin((PEntity<PE>) j.getEntity(), (PEntity<PE>) j.getAlias());
				} else if (j.getEntity() != null && j.getAlias() == null) {
					q = q.leftJoin((PEntity<PE>) j.getEntity());
				}

				if (j.isFetch()) {
					q = q.fetch();
				}
			}
		}
		if (this.where != null && this.where.size() > 0) {
			for (EBoolean w : this.where) {
				q = q.where(w);
			}
		}
		if (this.order != null && this.order.size() > 0) {
			for (OrderSpecifier o : this.order) {
				q = q.orderBy(o);
			}
		}

		if (this.group != null && this.group.size() > 0) {
			for (Expr g : this.group) {
				q = q.groupBy(g);
			}
		}

		return q;
	}

	@Override
	public QueryBuilder clear() {
		this.from.clear();
		this.join.clear();
		this.order.clear();
		this.where.clear();
		this.group.clear();
		return this;
	}

	@Override
	public QueryBuilder setLimit(int limit) {
		this.limit = limit;
		return this;
	}

	@Override
	public QueryBuilder setOffset(int offset) {
		this.offset = offset;
		return this;
	}

	@Override
	public QueryBuilder addGroup(Expr group) {
		if (group != null) {
			this.group.add(group);
		}
		return this;
	}
}
