/*
 * Copyright 2021 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.search.index.mapping.fieldmapper.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import io.jmix.search.index.mapping.ExtendedSearchSettings;
import io.jmix.search.index.mapping.ParameterKeys;
import io.jmix.search.utils.ExtendedSearchSubFieldsApplier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component("search_EnumFieldMapper")
public class EnumFieldMapper extends SimpleFieldMapper {

    protected static final Set<String> supportedParameters = Collections.unmodifiableSet(
            Sets.newHashSet(ParameterKeys.ANALYZER)
    );

    @Override
    boolean isExtendedSearchSupported() {
        return true;
    }

    @Override
    protected ObjectNode applyExtendedSearch(ObjectNode config, ExtendedSearchSettings extendedSearchSettings) {
        return ExtendedSearchSubFieldsApplier.applyPrefixSubField(config, extendedSearchSettings);
    }

    @Override
    protected String getSearchPlatformDatatype() {
        return "text";
    }

    @Override
    public Set<String> getSupportedMappingParameters() {
        return supportedParameters;
    }
}
