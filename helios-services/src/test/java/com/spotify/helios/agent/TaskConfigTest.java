package com.spotify.helios.agent;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ImageInfo;
import com.spotify.helios.common.descriptors.HealthCheck;
import com.spotify.helios.common.descriptors.Job;
import com.spotify.helios.common.descriptors.PortMapping;
import com.spotify.helios.common.descriptors.ServiceEndpoint;
import com.spotify.helios.common.descriptors.ServicePorts;
import com.spotify.helios.serviceregistration.ServiceRegistration;
import com.spotify.helios.serviceregistration.ServiceRegistration.EndpointHealthCheck;

public class TaskConfigTest {
  private static final String HOST = "HOST";
  private static final String IMAGE = "spotify:17";
  private static final String PORT_NAME = "default-port";
  private static final int EXTERNAL_PORT = 20000;
  private static final Job JOB = Job.newBuilder()
    .setName("foobar")
    .setCommand(asList("foo", "bar"))
    .setImage(IMAGE)
    .setVersion("4711")
    .addPort(PORT_NAME, PortMapping.of(8080, EXTERNAL_PORT))
    .addRegistration(ServiceEndpoint.of("service", "http"), ServicePorts.of(PORT_NAME))
    .build();

  @Test
  public void testRegistrationWithHttpHealthCheck() throws Exception {
    final String path = "/health";

    final Job job = JOB.toBuilder()
      .setHealthCheck(HealthCheck.newHttpHealthCheck()
        .setPath(path)
        .setPort(PORT_NAME).build())
      .build();

    final TaskConfig taskConfig = TaskConfig.builder()
      .namespace("test")
      .host(HOST)
      .job(job)
      .build();

    ServiceRegistration.Endpoint endpoint = taskConfig.registration().getEndpoints().get(0);
    assertEquals(path, endpoint.getHealthCheck().getPath());
    assertEquals(EndpointHealthCheck.HTTP, endpoint.getHealthCheck().getType());
    assertEquals(EXTERNAL_PORT, endpoint.getPort());
  }

  @Test
  public void testRegistrationWithTcpHealthCheck() throws Exception {
    final Job job = JOB.toBuilder()
      .setHealthCheck(HealthCheck.newTcpHealthCheck()
        .setPort(PORT_NAME).build())
      .build();

    final TaskConfig taskConfig = TaskConfig.builder()
      .namespace("test")
      .host(HOST)
      .job(job)
      .build();

    ServiceRegistration.Endpoint endpoint = taskConfig.registration().getEndpoints().get(0);
    assertEquals(EndpointHealthCheck.TCP, endpoint.getHealthCheck().getType());
    assertEquals(EXTERNAL_PORT, endpoint.getPort());
  }

  @Test
  public void testRegistrationWithoutHealthCheck() throws Exception {
    final TaskConfig taskConfig = TaskConfig.builder()
      .namespace("test")
      .host(HOST)
      .job(JOB)
      .build();

    ServiceRegistration.Endpoint endpoint = taskConfig.registration().getEndpoints().get(0);
    assertNull(endpoint.getHealthCheck());
  }

  @Test
  public void testResolveCmd() throws Exception {
    final Map<String,String> lookup = ImmutableMap.of("arg3", "resolved_arg3");
    final Job job = JOB.toBuilder()
      .setCommand(asList("arg1", "${arg2}", "${arg3}"))
      .build();

    final TaskConfig taskConfig = TaskConfig.builder()
      .namespace("test")
      .host(HOST)
      .job(job)
      .build();

    ContainerConfig cfg = taskConfig.containerConfig(new ImageInfo(), lookup);
    assertEquals("arg1", cfg.cmd().get(0));
    // unresolved
    assertEquals("${arg2}", cfg.cmd().get(1));
    // resolved
    assertEquals("resolved_arg3", cfg.cmd().get(2));
  }
}