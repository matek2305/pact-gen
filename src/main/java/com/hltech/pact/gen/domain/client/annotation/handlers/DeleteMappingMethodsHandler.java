package com.hltech.pact.gen.domain.client.annotation.handlers;

import com.hltech.pact.gen.domain.client.annotation.MappingMethodHandler;
import com.hltech.pact.gen.domain.client.model.Param;
import com.hltech.pact.gen.domain.client.model.RequestRepresentation;
import com.hltech.pact.gen.domain.client.util.PathParametersExtractor;
import com.hltech.pact.gen.domain.client.util.RawHeadersParser;
import com.hltech.pact.gen.domain.client.util.RequestBodyExtractor;
import com.hltech.pact.gen.domain.client.util.RequestHeaderParamsExtractor;
import com.hltech.pact.gen.domain.client.util.RequestParametersExtractor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@MappingMethodHandler
public class DeleteMappingMethodsHandler implements AnnotatedMethodHandler {

    @Override
    public boolean isSupported(Method method) {
        return method.isAnnotationPresent(DeleteMapping.class);
    }

    @Override
    public RequestRepresentation handleRequest(Method method) {
        return RequestRepresentation.builder()
            .httpMethod(HttpMethod.DELETE)
            .path(getPathFromMethod(method))
            .headers(combineHeaders(
                ArrayUtils.addAll(method.getAnnotation(DeleteMapping.class).headers(), getRequestMediaHeaders(method)),
                RequestHeaderParamsExtractor.extractAll(method)))
            .body(RequestBodyExtractor.extract(method.getParameters()))
            .requestParameters(RequestParametersExtractor.extractAll(
                method.getParameters(),
                method.getAnnotation(DeleteMapping.class).path()))
            .pathParameters(PathParametersExtractor.extractAll(method))
            .build();
    }

    @Override
    public String[] getResponseMediaHeaders(Method method) {
        return Arrays.stream(method.getAnnotation(DeleteMapping.class).produces())
            .map(header -> HttpHeaders.CONTENT_TYPE + "=" + header)
            .toArray(String[]::new);
    }

    private String[] getRequestMediaHeaders(Method method) {
        return ArrayUtils.addAll(
            Arrays.stream(method.getAnnotation(DeleteMapping.class).consumes())
                .map(header -> HttpHeaders.CONTENT_TYPE + "=" + header)
                .toArray(String[]::new),
            Arrays.stream(method.getAnnotation(DeleteMapping.class).produces())
                .map(header -> HttpHeaders.ACCEPT + "=" + header)
                .toArray(String[]::new));
    }

    private String getPathFromMethod(Method method) {
        DeleteMapping annotation = method.getAnnotation(DeleteMapping.class);
        return annotation.path().length == 1
            ? annotation.path()[0].split("\\?")[0]
            : annotation.value()[0].split("\\?")[0];
    }

    private static List<Param> combineHeaders(String[] rawHeaders, List<Param> headers) {
        return Stream
            .concat(RawHeadersParser.parseAll(rawHeaders).stream(), headers.stream())
            .collect(Collectors.toList());
    }
}
