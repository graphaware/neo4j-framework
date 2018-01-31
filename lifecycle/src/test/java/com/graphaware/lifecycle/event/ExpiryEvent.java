/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.lifecycle.event;

import com.graphaware.common.log.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.Entity;
import org.neo4j.logging.Log;

public abstract class ExpiryEvent {

	private static final Log LOG = LoggerFactory.getLogger(ExpiryEvent.class);

	public static final String EXPIRY_OFFSET = "expiryOffset";

	protected Long expiryOffset;
	protected String expirationProperty;
	protected String ttlProperty;
	protected String indexName;


	protected Long getExpirationDate(Entity entity, String expirationProperty, String ttlProperty) {
		if (!(entity.hasProperty(expirationProperty) || entity.hasProperty(ttlProperty))) {
			return null;
		}

		Long result = null;

		if (entity.hasProperty(expirationProperty)) {
			try {
				long expiry = Double.valueOf((entity.getProperty(expirationProperty).toString())).longValue();
				result = expiry + this.expiryOffset;
			} catch (NumberFormatException e) {
				LOG.warn("%s expiration property is non-numeric: %s", entity.getId(), entity.getProperty(expirationProperty));
			}
		}

		if (entity.hasProperty(ttlProperty)) {
			try {
				long newResult = System.currentTimeMillis() + Long.parseLong(entity.getProperty(ttlProperty).toString());

				if (result != null) {
					LOG.warn("%s has both expiry date and a ttl.", entity.getId());

					if (newResult > result) {
						LOG.warn("Using ttl as it is later.");
						result = newResult;
					} else {
						LOG.warn("Using expiry date as it is later.");
					}
				} else {
					result = newResult;
				}
			} catch (NumberFormatException e) {
				LOG.warn("%s ttl property is non-numeric: %s", entity.getId(), entity.getProperty(ttlProperty));
			}
		}

		return result;
	}

	protected void validate() {
		if (indexName != null && StringUtils.equals(ttlProperty, expirationProperty)) {
			throw new IllegalStateException("Node TTL and expiration property are not allowed to be the same!");
		}
	}

}
