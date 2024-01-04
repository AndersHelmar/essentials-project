/*
 * Copyright 2021-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dk.cloudcreate.essentials.components.foundation.messaging.eip.store_and_forward;

import dk.cloudcreate.essentials.components.foundation.messaging.eip.store_and_forward.operation.InvokeMessageHandlerMethod;
import dk.cloudcreate.essentials.shared.interceptor.*;

public interface MessageHandlerInterceptor extends Interceptor {
    /**
     * Intercept {@link dk.cloudcreate.essentials.components.foundation.messaging.eip.store_and_forward.operation.InvokeMessageHandlerMethod} calls
     *
     * @param operation        the operation
     * @param interceptorChain the interceptor chain (call {@link InterceptorChain#proceed()} to continue the processing chain)
     */
    default void intercept(InvokeMessageHandlerMethod operation, InterceptorChain<InvokeMessageHandlerMethod, Void, MessageHandlerInterceptor> interceptorChain) {
        interceptorChain.proceed();
    }
}