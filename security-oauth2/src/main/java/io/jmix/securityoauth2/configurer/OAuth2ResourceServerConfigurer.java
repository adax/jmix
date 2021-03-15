/*
 * Copyright 2019 Haulmont.
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

package io.jmix.securityoauth2.configurer;

import io.jmix.securityoauth2.filter.LastSecurityFilter;
import io.jmix.securityoauth2.impl.RequestLocaleProvider;
import io.jmix.securityoauth2.token.TokenMasker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

import static io.jmix.security.SecurityConfigurers.apiSecurity;

public class OAuth2ResourceServerConfigurer extends ResourceServerConfigurerAdapter {
    private ApplicationEventPublisher applicationEventPublisher;
    private TokenMasker tokenMasker;
    private RequestLocaleProvider localeProvider;
    private LastSecurityFilter lastSecurityFilter;

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Autowired
    public void setTokenMasker(TokenMasker tokenMasker) {
        this.tokenMasker = tokenMasker;
    }

    @Autowired
    public void setLocaleProvider(RequestLocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    public ApplicationEventPublisher getApplicationEventPublisher() {
        return applicationEventPublisher;
    }

    public TokenMasker getTokenMasker() {
        return tokenMasker;
    }

    public RequestLocaleProvider getLocaleProvider() {
        return localeProvider;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.apply(apiSecurity())
                .sessionManagement()
                .and()
                .addFilterAfter(getLastSecurityFilter(), FilterSecurityInterceptor.class);
    }

    public LastSecurityFilter getLastSecurityFilter() {
        if (lastSecurityFilter == null) {
            lastSecurityFilter = new LastSecurityFilter();
            lastSecurityFilter.setApplicationEventPublisher(getApplicationEventPublisher());
            lastSecurityFilter.setLocaleProvider(getLocaleProvider());
            lastSecurityFilter.setTokenMasker(getTokenMasker());
        }
        return lastSecurityFilter;
    }
}
