// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.bidi.network;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.openqa.selenium.testing.Safely.safelyCall;
import static org.openqa.selenium.testing.drivers.Browser.EDGE;
import static org.openqa.selenium.testing.drivers.Browser.IE;
import static org.openqa.selenium.testing.drivers.Browser.SAFARI;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.bidi.Network;
import org.openqa.selenium.environment.webserver.AppServer;
import org.openqa.selenium.environment.webserver.NettyAppServer;
import org.openqa.selenium.testing.JupiterTestBase;
import org.openqa.selenium.testing.NotYetImplemented;
import org.openqa.selenium.testing.Pages;

class NetworkEventsTest extends JupiterTestBase {

  private String page;
  private AppServer server;

  @BeforeEach
  public void setUp() {
    server = new NettyAppServer();
    server.start();
  }

  @Test
  @NotYetImplemented(SAFARI)
  @NotYetImplemented(IE)
  @NotYetImplemented(EDGE)
  void canListenToBeforeRequestSentEvent()
      throws ExecutionException, InterruptedException, TimeoutException {
    try (Network network = new Network(driver)) {
      CompletableFuture<BeforeRequestSent> future = new CompletableFuture<>();
      network.onBeforeRequestSent(future::complete);
      page = server.whereIs("/bidi/logEntryAdded.html");
      driver.get(page);

      BeforeRequestSent requestSent = future.get(5, TimeUnit.SECONDS);
      String windowHandle = driver.getWindowHandle();
      assertThat(requestSent.getBrowsingContextId()).isEqualTo(windowHandle);
      assertThat(requestSent.getRequest().getRequestId()).isNotNull();
      assertThat(requestSent.getRequest().getMethod()).isEqualToIgnoringCase("get");
      assertThat(requestSent.getRequest().getUrl()).isNotNull();
      assertThat(requestSent.getInitiator().getType().toString()).isEqualToIgnoringCase("other");
    }
  }

  @Test
  @NotYetImplemented(SAFARI)
  @NotYetImplemented(IE)
  @NotYetImplemented(EDGE)
  void canListenToResponseStartedEvent()
      throws ExecutionException, InterruptedException, TimeoutException {
    try (Network network = new Network(driver)) {
      CompletableFuture<ResponseDetails> future = new CompletableFuture<>();
      network.onResponseStarted(future::complete);
      page = server.whereIs("/bidi/logEntryAdded.html");
      driver.get(page);

      ResponseDetails response = future.get(5, TimeUnit.SECONDS);
      String windowHandle = driver.getWindowHandle();
      assertThat(response.getBrowsingContextId()).isEqualTo(windowHandle);
      assertThat(response.getRequest().getRequestId()).isNotNull();
      assertThat(response.getRequest().getMethod()).isEqualToIgnoringCase("get");
      assertThat(response.getRequest().getUrl()).isNotNull();
      assertThat(response.getResponseData().getHeaders().size()).isGreaterThanOrEqualTo(1);
      assertThat(response.getResponseData().getUrl()).contains("/bidi/logEntryAdded.html");
      assertThat(response.getResponseData().getStatus()).isEqualTo(200L);
    }
  }

  @Test
  @NotYetImplemented(SAFARI)
  @NotYetImplemented(IE)
  @NotYetImplemented(EDGE)
  void canListenToResponseCompletedEvent()
      throws ExecutionException, InterruptedException, TimeoutException {
    try (Network network = new Network(driver)) {
      CompletableFuture<ResponseDetails> future = new CompletableFuture<>();
      network.onResponseCompleted(future::complete);
      page = server.whereIs("/bidi/logEntryAdded.html");
      driver.get(page);

      ResponseDetails response = future.get(5, TimeUnit.SECONDS);
      String windowHandle = driver.getWindowHandle();
      assertThat(response.getBrowsingContextId()).isEqualTo(windowHandle);
      assertThat(response.getRequest().getRequestId()).isNotNull();
      assertThat(response.getRequest().getMethod()).isEqualToIgnoringCase("get");
      assertThat(response.getRequest().getUrl()).isNotNull();
      assertThat(response.getResponseData().getHeaders().size()).isGreaterThanOrEqualTo(1);
      assertThat(response.getResponseData().getUrl()).contains("/bidi/logEntryAdded.html");
      assertThat(response.getResponseData().getStatus()).isEqualTo(200L);
    }
  }

  @Test
  @NotYetImplemented(SAFARI)
  @NotYetImplemented(IE)
  @NotYetImplemented(EDGE)
  void canListenToResponseCompletedEventWithCookie()
      throws ExecutionException, InterruptedException, TimeoutException {
    try (Network network = new Network(driver)) {
      CompletableFuture<BeforeRequestSent> future = new CompletableFuture<>();

      driver.get(new Pages(server).blankPage);
      driver.manage().addCookie(new Cookie("foo", "bar"));
      network.onBeforeRequestSent(future::complete);
      driver.navigate().refresh();

      BeforeRequestSent requestSent = future.get(5, TimeUnit.SECONDS);
      String windowHandle = driver.getWindowHandle();
      assertThat(requestSent.getBrowsingContextId()).isEqualTo(windowHandle);
      assertThat(requestSent.getRequest().getCookies().size()).isEqualTo(1);
      assertThat(requestSent.getRequest().getCookies().get(0).getName()).isEqualTo("foo");
      assertThat(requestSent.getRequest().getCookies().get(0).getValue().getValue())
          .isEqualTo("bar");
    }
  }

  @AfterEach
  public void quitDriver() {
    if (driver != null) {
      driver.quit();
    }
    safelyCall(server::stop);
  }
}
