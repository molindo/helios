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

package com.spotify.helios.master;

import com.spotify.helios.servicescommon.ServiceParser;

import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static net.sourceforge.argparse4j.impl.Arguments.append;

/**
 * Parses command-line arguments to produce the {@link MasterConfig}.
 */
public class MasterParser extends ServiceParser {

  private final MasterConfig masterConfig;

  private Argument httpArg;
  private Argument adminArg;
  private Argument kafkaArg;
  private Argument stateDirArg;

  public MasterParser(final String... args) throws ArgumentParserException {
    super("helios-master", "Spotify Helios Master", args);

    final Namespace options = getNamespace();
    final InetSocketAddress httpAddress = parseSocketAddress(options.getString(httpArg.getDest()));
    final List<String> kafkaBrokers = options.getList(kafkaArg.getDest());

    final MasterConfig config = new MasterConfig()
        .setZooKeeperConnectString(getZooKeeperConnectString())
        .setZooKeeperSessionTimeoutMillis(getZooKeeperSessionTimeoutMillis())
        .setZooKeeperConnectionTimeoutMillis(getZooKeeperConnectionTimeoutMillis())
        .setZooKeeperNamespace(getZooKeeperNamespace())
        .setZooKeeperClusterId(getZooKeeperClusterId())
        .setNoZooKeeperMasterRegistration(getNoZooKeeperRegistration())
        .setDomain(getDomain())
        .setName(getName())
        .setStatsdHostPort(getStatsdHostPort())
        .setRiemannHostPort(getRiemannHostPort())
        .setInhibitMetrics(getInhibitMetrics())
        .setSentryDsn(getSentryDsn())
        .setServiceRegistryAddress(getServiceRegistryAddress())
        .setServiceRegistrarPlugin(getServiceRegistrarPlugin())
        .setAdminPort(options.getInt(adminArg.getDest()))
        .setHttpEndpoint(httpAddress)
        .setKafkaBrokers(kafkaBrokers)
        .setStateDirectory(Paths.get(options.getString(stateDirArg.getDest())));

    this.masterConfig = config;
  }

  @Override
  protected void addArgs(final ArgumentParser parser) {
    httpArg = parser.addArgument("--http")
        .setDefault("http://0.0.0.0:5801")
        .help("http endpoint");

    adminArg = parser.addArgument("--admin")
        .type(Integer.class)
        .setDefault(5802)
        .help("admin http port");

    kafkaArg = parser.addArgument("--kafka")
        .action(append())
        .setDefault(new ArrayList<String>())
        .help("Kafka brokers to bootstrap with");

    stateDirArg = parser.addArgument("--state-dir")
        .setDefault(".")
        .help("Directory for persisting agent state locally.");
  }

  public MasterConfig getMasterConfig() {
    return masterConfig;
  }
}
