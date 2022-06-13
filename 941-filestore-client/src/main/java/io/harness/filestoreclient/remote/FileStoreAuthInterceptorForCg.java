package io.harness.filestoreclient.remote;

import io.harness.delegate.DelegateAgentCommonVariables;
import io.harness.security.ServiceTokenGenerator;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class FileStoreAuthInterceptorForCg implements Interceptor {

    private String serviceSecret;
    private ServiceTokenGenerator serviceTokenGenerator;

    public FileStoreAuthInterceptorForCg(
            String serviceSecret,
            ServiceTokenGenerator serviceTokenGenerator
    ) {
        this.serviceSecret = serviceSecret;
        this.serviceTokenGenerator = serviceTokenGenerator;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = serviceTokenGenerator.getServiceToken(serviceSecret);
        Request request = chain.request();
        return chain.proceed(request.newBuilder()
                .header("Authorization", "Manager " + token)
                .build());
    }

}
