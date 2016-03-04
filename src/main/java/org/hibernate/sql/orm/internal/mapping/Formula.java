/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.orm.internal.mapping;

/**
 * @author Steve Ebersole
 */
public class Formula implements Value {
	private final AbstractTable table;
	private final String expression;
	private final int jdbcType;

	public Formula(AbstractTable table, String expression, int jdbcType) {
		this.table = table;
		this.expression = expression;
		this.jdbcType = jdbcType;
	}

	public String getExpression() {
		return expression;
	}

	@Override
	public TableReference getSourceTable() {
		return table;
	}

	@Override
	public int getJdbcType() {
		return jdbcType;
	}

	@Override
	public String toLoggableString() {
		return null;
	}
}