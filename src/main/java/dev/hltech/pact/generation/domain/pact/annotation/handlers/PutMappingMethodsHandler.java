package dev.hltech.pact.generation.domain.pact.annotation.handlers;

import dev.hltech.pact.generation.domain.client.model.Param;
import dev.hltech.pact.generation.domain.client.model.RequestProperties;
import dev.hltech.pact.generation.domain.pact.annotation.handlers.util.RawHeadersParser;
import dev.hltech.pact.generation.domain.pact.annotation.handlers.util.RequestBodyTypeFinder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PutMappingMethodsHandler implements AnnotationHandler {

    @Override
    public boolean isSupported(Method method) {
        return method.isAnnotationPresent(PutMapping.class);
    }

    @Override
    public RequestProperties handle(Method method) {
        return RequestProperties.builder()
            .httpMethod(HttpMethod.PUT)
            .path(method.getAnnotation(PutMapping.class).path()[0])
            .headers(combineHeaders(
                method.getAnnotation(PutMapping.class).headers(),
                extractRequestHeaderParams(method)))
            .bodyType(RequestBodyTypeFinder.findRequestBodyType(method.getParameters()))
            .requestParameters(extractRequestParameters(method))
            .pathParameters(extractPathParameters(method))
            .build();
    }

    private static List<Param> combineHeaders(String[] rawHeaders, List<Param> headers) {
        return Stream
            .concat(RawHeadersParser.parseHeaders(rawHeaders).stream(), headers.stream())
            .collect(Collectors.toList());
    }

    private static List<Param> extractRequestHeaderParams(Method feignClientMethod) {
        return Arrays.stream(feignClientMethod.getParameters())
            .filter(param -> param.getAnnotation(RequestHeader.class) != null)
            .filter(param -> param.getType() != Map.class
                && param.getType() != MultiValueMap.class
                && param.getType() != HttpHeaders.class)
            .map(PutMappingMethodsHandler::extractRequestHeaderParam)
            .collect(Collectors.toList());
    }

    private static Param extractRequestHeaderParam(Parameter param) {
        Param.ParamBuilder builder = Param.builder();

        extractHeaderDefaultValue(param).ifPresent(builder::defaultValue);

        return builder
            .name(extractHeaderName(param))
            .type(param.getType())
            .build();
    }

    private static Optional<Object> extractHeaderDefaultValue(Parameter param) {
        RequestHeader annotation = param.getAnnotation(RequestHeader.class);

        if (annotation.defaultValue().equals(ValueConstants.DEFAULT_NONE) || annotation.defaultValue().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(annotation.defaultValue());
    }

    private static String extractHeaderName(Parameter param) {
        RequestHeader annotation = param.getAnnotation(RequestHeader.class);

        if (!annotation.name().isEmpty()) {
            return annotation.name();
        } else if (!annotation.value().isEmpty()) {
            return annotation.value();
        }

        return param.getName();
    }

    private static List<Param> extractRequestParameters(Method feignClientMethod) {
        return Arrays.stream(feignClientMethod.getParameters())
            .filter(param -> param.getAnnotation(RequestParam.class) != null)
            .filter(param -> param.getType() != Map.class)
            .map(PutMappingMethodsHandler::extractRequestParameter)
            .collect(Collectors.toList());
    }

    private static Param extractRequestParameter(Parameter param) {
        Param.ParamBuilder builder = Param.builder();

        extractParamDefaultValue(param).ifPresent(builder::defaultValue);

        return builder
            .name(extractParamName(param))
            .type(param.getType())
            .build();
    }

    private static Optional<Object> extractParamDefaultValue(Parameter param) {
        RequestParam annotation = param.getAnnotation(RequestParam.class);

        if (annotation.defaultValue().equals(ValueConstants.DEFAULT_NONE) || annotation.defaultValue().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(annotation.defaultValue());
    }

    private static String extractParamName(Parameter param) {
        RequestParam annotation = param.getAnnotation(RequestParam.class);

        if (!annotation.name().isEmpty()) {
            return annotation.name();
        } else if (!annotation.value().isEmpty()) {
            return annotation.value();
        }

        return param.getName();
    }

    private static List<Param> extractPathParameters(Method feignClientMethod) {
        return Arrays.stream(feignClientMethod.getParameters())
            .filter(param -> param.getAnnotation(PathVariable.class) != null)
            .filter(param -> param.getType() != Map.class)
            .map(PutMappingMethodsHandler::extractPathParameter)
            .collect(Collectors.toList());
    }

    private static Param extractPathParameter(Parameter param) {
        PathVariable annotation = param.getAnnotation(PathVariable.class);

        return Param.builder()
            .name(annotation.name().isEmpty() ? annotation.value() : annotation.name())
            .type(param.getType())
            .build();
    }
}
