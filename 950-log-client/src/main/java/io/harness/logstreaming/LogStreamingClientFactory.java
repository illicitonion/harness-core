/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.logstreaming;

import io.harness.network.Http;
import io.harness.security.X509KeyManagerBuilder;
import io.harness.security.X509SslContextBuilder;
import io.harness.security.X509TrustManagerBuilder;
import io.harness.serializer.kryo.KryoConverterFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class LogStreamingClientFactory implements Provider<LogStreamingClient> {
  @Inject private KryoConverterFactory kryoConverterFactory;

  private final String logStreamingServiceBaseUrl;
  private final String clientCertificateFilePath;
  private final String clientCertificateKeyFilePath;

  public LogStreamingClientFactory(
      String logStreamingServiceBaseUrl, String clientCertificateFilePath, String clientCertificateKeyFilePath) {
    this.logStreamingServiceBaseUrl = logStreamingServiceBaseUrl;
    this.clientCertificateFilePath = clientCertificateFilePath;
    this.clientCertificateKeyFilePath = clientCertificateKeyFilePath;
  }

  @Override
  public LogStreamingClient get() {
    if (StringUtils.isBlank(logStreamingServiceBaseUrl)) {
      return null;
    }

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.registerModule(new Jdk8Module());
    objectMapper.registerModule(new GuavaModule());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    Retrofit retrofit = new Retrofit.Builder()
                            .client(getSafeOkHttpClient())
                            .baseUrl(logStreamingServiceBaseUrl)
                            .addConverterFactory(kryoConverterFactory)
                            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                            .build();

    return retrofit.create(LogStreamingClient.class);
  }

  private OkHttpClient getSafeOkHttpClient() {
    try {
      X509TrustManager trustManager = new X509TrustManagerBuilder().trustDefaultTrustStore().build();
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
          .connectTimeout(5, TimeUnit.SECONDS)
          .readTimeout(10, TimeUnit.SECONDS)
          .retryOnConnectionFailure(true)
          .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
          .build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
