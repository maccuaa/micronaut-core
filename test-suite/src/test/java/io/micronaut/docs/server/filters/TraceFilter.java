/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.docs.server.filters;

// tag::imports[]

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ResponseFilter;
import io.micronaut.http.annotation.ServerFilter;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
// end::imports[]

@Requires(property = "spec.filter", value = "TraceFilter")
// tag::class[]
@ServerFilter("/hello/**") // <1>
public class TraceFilter {

    private final TraceService traceService;

    public TraceFilter(TraceService traceService) { // <2>
        this.traceService = traceService;
    }
// end::class[]

    // tag::doFilter[]
    @RequestFilter
    @ExecuteOn(TaskExecutors.BLOCKING) // <2>
    public void filterRequest(HttpRequest<?> request) {
        traceService.trace(request); // <1>
    }

    @ResponseFilter
    public void filterResponse(MutableHttpResponse<?> res) {
        res.getHeaders().add("X-Trace-Enabled", "true"); // <3>
    }
    // end::doFilter[]
// tag::endclass[]
}
// end::endclass[]
