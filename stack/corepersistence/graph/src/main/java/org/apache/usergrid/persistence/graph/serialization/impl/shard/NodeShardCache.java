/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.usergrid.persistence.graph.serialization.impl.shard;


import java.util.Iterator;
import java.util.UUID;

import org.apache.usergrid.persistence.core.scope.OrganizationScope;
import org.apache.usergrid.persistence.model.entity.Id;


/**
 *  Cache implementation for returning versions based on the slice.  This shard may be latent.  As a result
 *  the allocation of new shards should be 2*shard timeout in the future.
 *
 */
public interface NodeShardCache {


    /**
     * Get the time meta data for the given node
     * @param nodeId
     * @param version The time to select the slice for.
     * @param edgeType
     */
    public long getSlice(final OrganizationScope scope, final Id nodeId, final UUID version, final String... edgeType);

    /**
     * Get an iterator of all versions <= the version
     * @param scope
     * @param nodeId
     * @param maxVersion
     * @param edgeType
     * @return
     */
    public Iterator<Long> getVersions(final OrganizationScope scope, final Id nodeId, final UUID maxVersion, final String... edgeType);


    /**
     * Increment the cached counter amount for this shard
     * @param scope
     * @param nodeId
     * @param shard
     * @param count
     * @return The new local cached count
     */
    public long increment(final OrganizationScope scope, final Id nodeId, final long shard, final long count, final String... edgeTypes);
}
