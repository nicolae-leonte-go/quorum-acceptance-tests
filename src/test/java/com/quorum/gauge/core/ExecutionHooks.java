/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.quorum.gauge.core;

import com.quorum.gauge.services.UtilService;
import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.BeforeScenario;
import com.thoughtworks.gauge.ExecutionContext;
import com.thoughtworks.gauge.datastore.DataStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class ExecutionHooks {
    private static Logger logger = LoggerFactory.getLogger(ExecutionHooks.class);

    @Autowired
    UtilService utilService;

    @BeforeScenario(tags = "!isolate")
    public void saveCurrentBlockNumber() {
        BigInteger currentBlockNumber = utilService.getCurrentBlockNumber().toBlocking().first().getBlockNumber();
        DataStoreFactory.getScenarioDataStore().put("blocknumber", currentBlockNumber);
    }

    @AfterScenario(tags = "network-cleanup-required")
    public void cleanUpNetwork(ExecutionContext context) {
        String networkName = (String) DataStoreFactory.getScenarioDataStore().get("networkName");
        logger.debug("Cleaning up network {}", networkName);
    }
}
