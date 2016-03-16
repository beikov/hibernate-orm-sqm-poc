/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.EntityMode;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.sql.exec.internal.RecommendedJdbcTypeMappings;
import org.hibernate.sql.exec.results.spi.ResultSetProcessingOptions;
import org.hibernate.sql.exec.results.spi.ReturnReader;
import org.hibernate.sql.exec.results.spi.RowProcessingState;
import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.type.CompositeType;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.JavaTypeDescriptorRegistry;

/**
 * @author Steve Ebersole
 */
public abstract class SelfReadingExpressionSupport implements Expression, ReturnReader {
	@Override
	public ReturnReader getReturnReader() {
		return this;
	}

	@Override
	public void readBasicValues(
			RowProcessingState processingState,
			ResultSetProcessingOptions options) throws SQLException {
		throw new NotYetImplementedException();
	}

	@Override
	public void resolveBasicValues(
			RowProcessingState processingState,
			ResultSetProcessingOptions options) throws SQLException {
		throw new NotYetImplementedException();
	}

	@Override
	public Object assemble(
			RowProcessingState processingState,
			ResultSetProcessingOptions options) throws SQLException {
		throw new NotYetImplementedException();
	}

	@Override
	public int getNumberOfColumnsRead(RowProcessingState processingState) {
		assert getType() != null;
		return getType().getColumnSpan( processingState.getResultSetProcessingState().getSession().getFactory() );
	}

	@Override
	public Class getReturnedJavaType() {
		assert getType() != null;
		return getType().getReturnedClass();
	}

	@Override
	public Object readResult(
			RowProcessingState processingState,
			ResultSetProcessingOptions options,
			int startPosition,
			Object owner) throws SQLException {
		// for now we assume basic types with no attribute conversion etc.
		// a more correct implementation requires the "positional read" changes to Type.
		assert getType() != null;

		final SessionImplementor session = processingState.getResultSetProcessingState().getSession();
		final ResultSet resultSet = processingState.getResultSetProcessingState().getResultSet();

		final int columnSpan = getType().getColumnSpan( session.getFactory() );
		final int[] jdbcTypes = getType().sqlTypes( session.getFactory() );
		if ( columnSpan > 1 ) {
			// has to be a CompositeType for now (and a very basic, one-level one)...
			final CompositeType ctype = (CompositeType) getType();
			final Object[] values = new Object[ columnSpan ];
			for ( int i = 0; i < columnSpan; i++ ) {
				values[i] = readResultValue( resultSet, startPosition+i, jdbcTypes[i] );
			}
			try {
				final Object result = ctype.getReturnedClass().newInstance();
				ctype.setPropertyValues( result, values, EntityMode.POJO );
				return result;
			}
			catch (Exception e) {
				throw new RuntimeException( "Unable to instantiate composite : " +  ctype.getReturnedClass().getName(), e );
			}
		}
		else {
			return readResultValue( resultSet, startPosition, jdbcTypes[0] );
		}
	}

	private Object readResultValue(ResultSet resultSet, int position, int jdbcType) throws SQLException {
		final Class javaClassMapping = RecommendedJdbcTypeMappings.INSTANCE.determineJavaClassForJdbcTypeCode( jdbcType );
		final JavaTypeDescriptor javaTypeDescriptor = JavaTypeDescriptorRegistry.INSTANCE.getDescriptor( javaClassMapping );

		switch ( jdbcType ) {
			case Types.BIGINT: {
				return javaTypeDescriptor.wrap( resultSet.getLong( position ), null );
			}
			case Types.BIT: {
				return javaTypeDescriptor.wrap( resultSet.getBoolean( position ), null );
			}
			case Types.BOOLEAN: {
				return javaTypeDescriptor.wrap( resultSet.getBoolean( position ), null );
			}
			case Types.CHAR: {
				return javaTypeDescriptor.wrap( resultSet.getString( position ), null );
			}
			case Types.DATE: {
				return javaTypeDescriptor.wrap( resultSet.getDate( position ), null );
			}
			case Types.DECIMAL: {
				return javaTypeDescriptor.wrap( resultSet.getBigDecimal( position ), null );
			}
			case Types.DOUBLE: {
				return javaTypeDescriptor.wrap( resultSet.getDouble( position ), null );
			}
			case Types.FLOAT: {
				return javaTypeDescriptor.wrap( resultSet.getFloat( position ), null );
			}
			case Types.INTEGER: {
				return javaTypeDescriptor.wrap( resultSet.getInt( position ), null );
			}
			case Types.LONGNVARCHAR: {
				return javaTypeDescriptor.wrap( resultSet.getString( position ), null );
			}
			case Types.LONGVARCHAR: {
				return javaTypeDescriptor.wrap( resultSet.getString( position ), null );
			}
			case Types.LONGVARBINARY: {
				return javaTypeDescriptor.wrap( resultSet.getBytes( position ), null );
			}
			case Types.NCHAR: {
				return javaTypeDescriptor.wrap( resultSet.getString( position ), null );
			}
			case Types.NUMERIC: {
				return javaTypeDescriptor.wrap( resultSet.getBigDecimal( position ), null );
			}
			case Types.NVARCHAR: {
				return javaTypeDescriptor.wrap( resultSet.getString( position ), null );
			}
			case Types.TIME: {
				return javaTypeDescriptor.wrap( resultSet.getTime( position ), null );
			}
			case Types.TIMESTAMP: {
				return javaTypeDescriptor.wrap( resultSet.getTimestamp( position ), null );
			}
			case Types.VARCHAR: {
				return javaTypeDescriptor.wrap( resultSet.getString( position ), null );
			}
		}

		throw new UnsupportedOperationException( "JDBC type [" + jdbcType + " not supported" );
	}
}