/*
 * Copyright (c) 2014 Spotify AB.
 *
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

package com.spotify.helios.rollingupdate;

import com.google.common.annotations.VisibleForTesting;

import com.spotify.helios.agent.KafkaClientProvider;
import com.spotify.helios.common.descriptors.DeploymentGroupEvent;
import com.spotify.helios.servicescommon.QueueingHistoryWriter;
import com.spotify.helios.servicescommon.coordination.Paths;
import com.spotify.helios.servicescommon.coordination.ZooKeeperClient;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Writes rolling update history to ZK.
 */
public class DeploymentGroupHistoryWriter extends QueueingHistoryWriter<DeploymentGroupEvent> {

  private static final String KAFKA_TOPIC = "HeliosEvents";

  @Override
  protected String getKey(final DeploymentGroupEvent event) {
    return event.getDeploymentGroup().getName();
  }

  @Override
  protected long getTimestamp(final DeploymentGroupEvent event) {
    return event.getTimestamp();
  }

  @Override
  protected String getKafkaTopic() {
    return KAFKA_TOPIC;
  }

  @Override
  protected String getZkEventsPath(final DeploymentGroupEvent event) {
    return Paths.historyDeploymentGroup(event.getDeploymentGroup());
  }

  @Override
  protected byte[] toBytes(final DeploymentGroupEvent deploymentGroupEvent) {
    return new byte[0];
  }

  public DeploymentGroupHistoryWriter(final ZooKeeperClient client,
                                      final KafkaClientProvider kafkaProvider,
                                      final Path backingFile)
      throws IOException, InterruptedException {
    super(client, backingFile, kafkaProvider);
  }

  public void saveHistoryItem(final DeploymentGroupEvent event) throws InterruptedException {
    add(event);
  }

  public void saveHistoryItems(final List<DeploymentGroupEvent> events)
      throws InterruptedException {
    for (final DeploymentGroupEvent e : events) {
      add(e);
    }
  }

  @Override @VisibleForTesting
  protected void startUp() throws Exception {
    super.startUp();
  }
}
