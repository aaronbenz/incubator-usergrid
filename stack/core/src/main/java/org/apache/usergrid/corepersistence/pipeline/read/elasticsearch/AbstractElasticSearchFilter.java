/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.usergrid.corepersistence.pipeline.read.elasticsearch;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.usergrid.corepersistence.pipeline.cursor.CursorSerializer;
import org.apache.usergrid.corepersistence.pipeline.read.AbstractSeekingFilter;
import org.apache.usergrid.corepersistence.pipeline.read.CandidateResultsFilter;
import org.apache.usergrid.persistence.core.metrics.MetricsFactory;
import org.apache.usergrid.persistence.core.metrics.ObservableTimer;
import org.apache.usergrid.persistence.index.ApplicationEntityIndex;
import org.apache.usergrid.persistence.index.CandidateResults;
import org.apache.usergrid.persistence.index.EntityIndexFactory;
import org.apache.usergrid.persistence.index.SearchEdge;
import org.apache.usergrid.persistence.index.SearchTypes;
import org.apache.usergrid.persistence.model.entity.Id;

import com.codahale.metrics.Timer;
import com.google.common.base.Optional;

import rx.Observable;


/**
 * Command for reading graph edges
 */
public abstract class AbstractElasticSearchFilter extends AbstractSeekingFilter<Id, CandidateResults, Integer>
    implements CandidateResultsFilter {

    private static final Logger log = LoggerFactory.getLogger( AbstractElasticSearchFilter.class );

    private final EntityIndexFactory entityIndexFactory;
    private final String query;
    private final Timer searchTimer;


    /**
     * Create a new instance of our command
     */
    public AbstractElasticSearchFilter( final EntityIndexFactory entityIndexFactory,
                                        final MetricsFactory metricsFactory, final String query ) {
        this.entityIndexFactory = entityIndexFactory;
        this.query = query;
        this.searchTimer = metricsFactory.getTimer( AbstractElasticSearchFilter.class, "query" );
    }


    @Override
    public Observable<CandidateResults> call( final Observable<Id> observable ) {

        //get the graph manager
        final ApplicationEntityIndex applicationEntityIndex =
            entityIndexFactory.createApplicationEntityIndex( pipelineContext.getApplicationScope() );


        final int limit = pipelineContext.getLimit();


        final SearchTypes searchTypes = getSearchTypes();


        //return all ids that are emitted from this edge
        return observable.flatMap( id -> {

            final SearchEdge searchEdge = getSearchEdge( id );


            final Observable<CandidateResults> candidates = Observable.create( subscriber -> {

                //our offset to our start value.  This will be set the first time we emit
                //after we receive new ids, we want to reset this to 0
                //set our our constant state
                final Optional<Integer> startFromCursor = getSeekValue();

                final int startOffset = startFromCursor.or( 0 );

                int currentOffSet = startOffset;

                subscriber.onStart();

                //emit while we have values from ES
                while ( true ) {


                    try {
                        final CandidateResults candidateResults =
                            applicationEntityIndex.search( searchEdge, searchTypes, query, limit, currentOffSet );

                        currentOffSet += candidateResults.size();

                        //set the cursor for the next value
                        setCursor( currentOffSet );

                        /**
                         * No candidates, we're done
                         */
                        if ( candidateResults.size() == 0 ) {
                            subscriber.onCompleted();
                            return;
                        }

                        subscriber.onNext( candidateResults );
                    }
                    catch ( Throwable t ) {

                        log.error( "Unable to search candidates", t );
                        subscriber.onError( t );
                    }
                }
            } );


            //add a timer around our observable
            ObservableTimer.time( candidates, searchTimer );

            return candidates;
        } );
    }


    @Override
    protected CursorSerializer<Integer> getCursorSerializer() {
        return ElasticsearchCursorSerializer.INSTANCE;
    }


    /**
     * Get the search edge from the id
     */
    protected abstract SearchEdge getSearchEdge( final Id id );

    /**
     * Get the search types
     */
    protected abstract SearchTypes getSearchTypes();
}