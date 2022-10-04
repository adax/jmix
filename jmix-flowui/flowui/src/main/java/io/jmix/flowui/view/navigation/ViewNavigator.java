/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.view.navigation;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.view.View;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class ViewNavigator {

    protected final Consumer<ViewNavigator> handler;

    protected String viewId;
    protected Class<? extends View> viewClass;

    protected RouteParameters routeParameters;
    protected QueryParameters queryParameters;

    protected Class<? extends View> backNavigationTarget;

    public ViewNavigator(Consumer<? extends ViewNavigator> handler) {
        checkNotNullArgument(handler);

        this.handler = (Consumer<ViewNavigator>) handler;
    }

    public ViewNavigator withViewId(@Nullable String viewId) {
        this.viewId = viewId;
        return this;
    }

    public ViewNavigator withViewClass(@Nullable Class<? extends View> viewClass) {
        this.viewClass = viewClass;
        return this;
    }

    public ViewNavigator withRouteParameters(@Nullable RouteParameters routeParameters) {
        this.routeParameters = routeParameters;
        return this;
    }

    public ViewNavigator withQueryParameters(@Nullable QueryParameters queryParameters) {
        this.queryParameters = queryParameters;
        return this;
    }

    public ViewNavigator withBackNavigationTarget(@Nullable Class<? extends View> backNavigationTarget) {
        this.backNavigationTarget = backNavigationTarget;
        return this;
    }

    public Optional<String> getViewId() {
        return Optional.ofNullable(viewId);
    }

    public Optional<Class<? extends View>> getViewClass() {
        return Optional.ofNullable(viewClass);
    }

    public Optional<RouteParameters> getRouteParameters() {
        return Optional.ofNullable(routeParameters);
    }

    public Optional<QueryParameters> getQueryParameters() {
        return Optional.ofNullable(queryParameters);
    }

    public Optional<Class<? extends View>> getBackNavigationTarget() {
        return Optional.ofNullable(backNavigationTarget);
    }

    public void navigate() {
        handler.accept(this);
    }
}
