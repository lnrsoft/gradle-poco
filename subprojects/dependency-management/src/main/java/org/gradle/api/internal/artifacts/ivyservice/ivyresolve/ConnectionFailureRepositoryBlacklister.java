/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.artifacts.ivyservice.ivyresolve;

import com.google.common.collect.Sets;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.InterruptedIOException;
import java.util.Collection;
import java.util.Set;

public class ConnectionFailureRepositoryBlacklister implements RepositoryBlacklister {

    private final Set<String> blacklistedRepositories = Sets.newConcurrentHashSet();

    @Override
    public boolean isBlacklisted(String repositoryId) {
        return blacklistedRepositories.contains(repositoryId);
    }

    @Override
    public boolean blacklistRepository(String repositoryId, Throwable throwable) {
        boolean blacklisted = isBlacklisted(repositoryId);

        if (blacklisted) {
            return true;
        }

        if (isCriticalFailure(throwable)) {
            blacklistedRepositories.add(repositoryId);
            return true;
        }

        return false;
    }

    @Override
    public Set<String> getBlacklistedRepositories() {
        return blacklistedRepositories;
    }

    public static boolean hasCriticalFailure(Collection<? extends Throwable> failures) {
        for (Throwable failure : failures) {
            if (isCriticalFailure(failure)) {
                return true;
            }
        }
        return false;
    }
    public static boolean isCriticalFailure(Throwable throwable) {
        return isTimeoutException(throwable);
    }
    private static boolean isTimeoutException(Throwable throwable) {
        Throwable rootCause = ExceptionUtils.getRootCause(throwable);
        //http://hc.apache.org/httpclient-3.x/exception-handling.html
        return rootCause instanceof InterruptedIOException;
    }
}
