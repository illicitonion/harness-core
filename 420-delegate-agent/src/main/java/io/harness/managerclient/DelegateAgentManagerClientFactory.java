/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.managerclient;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.network.FibonacciBackOff;
import io.harness.network.Http;
import io.harness.network.NoopHostnameVerifier;
import io.harness.security.TokenGenerator;
import io.harness.security.X509KeyManagerBuilder;
import io.harness.security.X509SslContextBuilder;
import io.harness.security.X509TrustManagerBuilder;
import io.harness.serializer.kryo.KryoConverterFactory;
import io.harness.version.VersionInfoManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Slf4j
@OwnedBy(HarnessTeam.DEL)
public class DelegateAgentManagerClientFactory
    implements Provider<io.harness.managerclient.DelegateAgentManagerClient> {
  @Inject private VersionInfoManager versionInfoManager;
  @Inject private KryoConverterFactory kryoConverterFactory;

  private final String baseUrl;
  private final TokenGenerator tokenGenerator;
  private final String clientCertificateFilePath;
  private final String clientCertificateKeyFilePath;

  DelegateAgentManagerClientFactory(String baseUrl, TokenGenerator tokenGenerator, String clientCertificateFilePath,
      String clientCertificateKeyFilePath) {
    this.baseUrl = baseUrl;
    this.tokenGenerator = tokenGenerator;
    this.clientCertificateFilePath = clientCertificateFilePath;
    this.clientCertificateKeyFilePath = clientCertificateKeyFilePath;
  }

  @Override
  public io.harness.managerclient.DelegateAgentManagerClient get() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new Jdk8Module());
    objectMapper.registerModule(new GuavaModule());
    objectMapper.registerModule(new JavaTimeModule());
    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(this.baseUrl)
                            .client(getSafeOkHttpClient())
                            .addConverterFactory(this.kryoConverterFactory)
                            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                            .build();
    return retrofit.create(io.harness.managerclient.DelegateAgentManagerClient.class);
  }

  private OkHttpClient getSafeOkHttpClient() {
    try {
      //      X509TrustManager trustManager = new X509TrustManagerBuilder().trustDefaultTrustStore().build();
      X509TrustManager trustManager = new X509TrustManagerBuilder().trustAllCertificates().build();
      X509SslContextBuilder sslContextBuilder = new X509SslContextBuilder().trustManager(trustManager);

      if (StringUtils.isNotEmpty(this.clientCertificateFilePath)
          && StringUtils.isNotEmpty(this.clientCertificateKeyFilePath)) {
        X509KeyManager keyManager =
            new X509KeyManagerBuilder()
                .withClientCertificateFromFile(this.clientCertificateFilePath, this.clientCertificateKeyFilePath)
                .build();
        sslContextBuilder.keyManager(keyManager);
      }

      SSLContext sslContext = sslContextBuilder.build();

      return Http.getOkHttpClientWithProxyAuthSetup()
          .hostnameVerifier(new NoopHostnameVerifier())
          .connectionPool(Http.connectionPool)
          .retryOnConnectionFailure(true)
          .addInterceptor(new io.harness.managerclient.DelegateAuthInterceptor(this.tokenGenerator))
          .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
          .addInterceptor(chain -> {
            Builder request = chain.request().newBuilder().addHeader(
                "User-Agent", "delegate/" + this.versionInfoManager.getVersionInfo().getVersion());
            return chain.proceed(request.build());
          })
          .addInterceptor(chain -> FibonacciBackOff.executeForEver(() -> chain.proceed(chain.request())))
          // During this call we not just query the task but we also obtain the secret on the manager side
          // we need to give enough time for the call to finish.
          .readTimeout(2, TimeUnit.MINUTES)
          .build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Let's keep this method for now, since we might want to give the options to customers to keep the unsafe way of
   * ignoring all certificate issues
   * @return
   */
  private OkHttpClient getUnsafeOkHttpClient() {
    try {
      X509TrustManager trustManager = new X509TrustManagerBuilder().trustAllCertificates().build();
      X509SslContextBuilder sslContextBuilder = new X509SslContextBuilder().trustManager(trustManager);

      if (StringUtils.isNotEmpty(this.clientCertificateFilePath)
          && StringUtils.isNotEmpty(this.clientCertificateKeyFilePath)) {
        X509KeyManager keyManager =
            new X509KeyManagerBuilder()
                .withClientCertificateFromFile(this.clientCertificateFilePath, this.clientCertificateKeyFilePath)
                .build();
        sslContextBuilder.keyManager(keyManager);
      }

      SSLContext sslContext = sslContextBuilder.build();

      return Http.getOkHttpClientWithProxyAuthSetup()
          .connectionPool(new ConnectionPool())
          .retryOnConnectionFailure(true)
          .addInterceptor(new io.harness.managerclient.DelegateAuthInterceptor(this.tokenGenerator))
          .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
          .addInterceptor(chain -> {
            Builder request = chain.request().newBuilder().addHeader(
                "User-Agent", "delegate/" + this.versionInfoManager.getVersionInfo().getVersion());
            return chain.proceed(request.build());
          })
          .addInterceptor(chain -> FibonacciBackOff.executeForEver(() -> chain.proceed(chain.request())))
          .hostnameVerifier(new NoopHostnameVerifier())
          // During this call we not just query the task but we also obtain the secret on the manager side
          // we need to give enough time for the call to finish.
          .readTimeout(2, TimeUnit.MINUTES)
          .build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
