/*******************************************************************************
 * Copyright 2012 Apigee Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.usergrid.services;


import org.junit.Rule;
import org.usergrid.ServiceApplication;
import org.usergrid.ServiceITSetup;
import org.usergrid.ServiceITSetupImpl;
import org.usergrid.ServiceITSuite;
import org.usergrid.cassandra.ClearShiroSubject;
import org.usergrid.cassandra.Concurrent;


@Concurrent()
public abstract class AbstractServiceIT
{
    @Rule
    public ClearShiroSubject clearShiroSubject = new ClearShiroSubject();

    @Rule
    public ServiceITSetup setup = new ServiceITSetupImpl( ServiceITSuite.cassandraResource );

    @Rule
    public ServiceApplication app = new ServiceApplication( setup );
}
