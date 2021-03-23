package com.tjhelmuth.dispatcher;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

@UtilityClass
public class Dispatchers {
    private static final ServiceLoader<DispatcherProvider> providers = ServiceLoader.load(DispatcherProvider.class);

    public Optional<AlertDispatcher> ofType(String type, Map<String, Object> configuration){
        return providers.stream()
                .map(ServiceLoader.Provider::get)
                .filter(prov -> StringUtils.equalsIgnoreCase(prov.getType(), type))
                .findFirst()
                .map(provider -> provider.create(configuration));
    }
}
