/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.antlr.path;

import org.hibernate.hql.parser.JoinType;
import org.hibernate.hql.parser.ParsingContext;
import org.hibernate.hql.parser.SemanticException;
import org.hibernate.hql.parser.antlr.HqlParser;
import org.hibernate.hql.parser.model.EntityTypeDescriptor;
import org.hibernate.hql.parser.semantic.expression.AttributeReferenceExpression;
import org.hibernate.hql.parser.semantic.expression.EntityTypeExpression;
import org.hibernate.hql.parser.semantic.expression.FromElementReferenceExpression;
import org.hibernate.hql.parser.semantic.from.FromClause;
import org.hibernate.hql.parser.semantic.from.FromElement;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class AttributePathResolverBasicImpl extends AbstractAttributePathResolverImpl {
	private static final Logger log = Logger.getLogger( AttributePathResolverBasicImpl.class );

	private final FromClause fromClause;

	public AttributePathResolverBasicImpl(FromClause fromClause) {
		this.fromClause = fromClause;
	}

	@Override
	protected ParsingContext parsingContext() {
		return fromClause.getParsingContext();
	}

	@Override
	protected Object resolveSimplePathContext(HqlParser.SimplePathContext pathContext) {
		final String pathText = pathContext.dotIdentifierSequence().getText();
		final String[] parts = pathText.split( "\\." );

		final String rootPart = parts[0];

		// 1st level precedence : qualified-attribute-path
		if ( pathText.contains( "." ) ) {
			final FromElement aliasedFromElement = fromClause.findFromElementByAlias( rootPart );
			if ( aliasedFromElement != null ) {
				final FromElement lhs = buildIntermediateAttributePathJoins(
						aliasedFromElement,
						parts,
						1,
						JoinType.LEFT,
						false
				);
				return new AttributeReferenceExpression( lhs, parts[parts.length-1] );
			}
		}

		// 2nd level precedence : from-element alias
		if ( !pathText.contains( "." ) ) {
			final FromElement aliasedFromElement = fromClause.findFromElementByAlias( rootPart );
			if ( aliasedFromElement != null ) {
				return new FromElementReferenceExpression( aliasedFromElement );
			}
		}

		// 3rd level precedence : unqualified-attribute-path
		final FromElement root = fromClause.findFromElementWithAttribute( rootPart );
		if ( root != null ) {
			final FromElement lhs = buildIntermediateAttributePathJoins(
					root,
					parts,
					0,
					JoinType.LEFT,
					false
			);
			return new AttributeReferenceExpression( lhs, parts[parts.length-1] );
		}

		// 4th level precedence : entity-name
		EntityTypeDescriptor entityType = parsingContext().getModelMetadata().resolveEntityReference( pathText );
		if ( entityType != null ) {
			return new EntityTypeExpression( entityType );
		}

		// 5th level precedence : constant reference
		try {
			return resolveConstantExpression( pathText );
		}
		catch (SemanticException e) {
			log.debug( e.getMessage() );
		}

		// if we get here we had a problem interpreting the dot-ident sequence
		throw new SemanticException( "Could not interpret token : " + pathText );
	}

	@Override
	protected Object resolveIndexedPathContext(HqlParser.IndexedPathContext pathContext) {
		return null;
	}

	@Override
	protected Object resolveTreatedPathContext(HqlParser.TreatedPathContext pathContext) {
		return null;
	}
}
