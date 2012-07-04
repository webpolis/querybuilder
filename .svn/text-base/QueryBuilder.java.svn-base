package es.com.tuv.trip2.hibernate.querybuilder;

import java.util.List;

import com.mysema.query.types.Expr;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.PList;
import com.mysema.query.types.path.PSet;

@SuppressWarnings("rawtypes")
public interface QueryBuilder {

	QueryBuilder addFrom(PEntity from);

	QueryBuilder addFrom(PEntity from[]);

	<P> QueryBuilder addJoin(PSet<P> join, boolean fetch);

	<P> QueryBuilder addJoin(PSet<P> join, PEntity alias, boolean fetch);
	
	<P> QueryBuilder addJoin(PList<?,?> join, PEntity alias, boolean fetch);

	<P> QueryBuilder addJoin(PEntity<P> join, PEntity alias, boolean fetch);

	QueryBuilder addWhere(EBoolean where);

	QueryBuilder addWhere(EBoolean where[]);

	QueryBuilder addOrder(OrderSpecifier order);

	QueryBuilder addOrder(OrderSpecifier order[]);

	QueryBuilder setLimit(int limit);

	QueryBuilder setOffset(int offset);
	
	QueryBuilder addGroup(Expr group);

	List<Object[]> selectAll(Expr... column);

	<RT> List<RT> selectAll(Expr column);

	Object[] selectUnique(Expr... column);

	<RT> RT selectUnique(Expr column);

	QueryBuilder clear();

	<P> QueryBuilder addJoin(PEntity<P> join, boolean fetch);
}
