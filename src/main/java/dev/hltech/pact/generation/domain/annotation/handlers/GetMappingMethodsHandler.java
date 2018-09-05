package dev.hltech.pact.generation.domain.annotation.handlers;

import dev.hltech.pact.generation.domain.client.model.Param;
import dev.hltech.pact.generation.domain.client.model.RequestProperties;
import dev.hltech.pact.generation.domain.util.PathParametersExtractor;
import dev.hltech.pact.generation.domain.util.RawHeadersParser;
import dev.hltech.pact.generation.domain.util.RequestBodyTypeFinder;
import dev.hltech.pact.generation.domain.util.RequestHeaderParamsExtractor;
import dev.hltech.pact.generation.domain.util.RequestParametersExtractor;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GetMappingMethodsHandler implements AnnotatedMethodHandler {

    @Override
    public boolean isSupported(Method method) {
        return method.isAnnotationPresent(GetMapping.class);
    }

    @Override
    public RequestProperties handle(Method method) {
        return RequestProperties.builder()
            .httpMethod(HttpMethod.GET)
            .path(method.getAnnotation(GetMapping.class).path()[0])
            .headers(combineHeaders(
                method.getAnnotation(GetMapping.class).headers(),
                RequestHeaderParamsExtractor.extractAll(method)))
            .bodyType(RequestBodyTypeFinder.find(method.getParameters()))
            .requestParameters(RequestParametersExtractor.extractAll(method))
            .pathParameters(PathParametersExtractor.extractAll(method))
            .build();
    }

    private static List<Param> combineHeaders(String[] rawHeaders, List<Param> headers) {
        return Stream
            .concat(RawHeadersParser.parseAll(rawHeaders).stream(), headers.stream())
            .collect(Collectors.toList());
    }
}