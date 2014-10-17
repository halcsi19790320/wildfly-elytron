/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
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

package org.wildfly.security.auth;

import static org.wildfly.security.manager.WildFlySecurityManager.getClassLoaderPrivileged;

import java.net.SocketAddress;
import java.net.URI;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Iterator;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslClientFactory;

import org.wildfly.security.manager.StackInspector;
import org.wildfly.security.permission.ElytronPermission;
import org.wildfly.security.sasl.util.SaslFactories;

/**
 * A client for consuming authentication context configurations.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class AuthenticationContextConfigurationClient {

    private static final ElytronPermission CREATE_PERMISSION = new ElytronPermission("createAuthenticationContextConfigurationClient");

    /**
     * A reusable privileged action to create a new configuration client.
     */
    public static final PrivilegedAction<AuthenticationContextConfigurationClient> ACTION = new PrivilegedAction<AuthenticationContextConfigurationClient>() {
        public AuthenticationContextConfigurationClient run() {
            return new AuthenticationContextConfigurationClient();
        }
    };

    static final StackInspector STACK_INSPECTOR = StackInspector.getInstance();

    /**
     * Construct a new instance.
     *
     * @throws SecurityException if the caller does not have permission to instantiate this class
     */
    public AuthenticationContextConfigurationClient() throws SecurityException {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(CREATE_PERMISSION);
        }
    }

    /**
     * Get the authentication configuration which matches the given URI, or {@code null} if there is none.
     *
     * @param uri the URI to match
     * @param authenticationContext the authentication context to examine
     * @return the matching configuration
     */
    public AuthenticationConfiguration getAuthenticationConfiguration(URI uri, AuthenticationContext authenticationContext) {
        final int idx = authenticationContext.ruleMatching(uri);
        if (idx == -1) return null;
        return authenticationContext.getAuthenticationConfiguration(idx);
    }

    /**
     * Get an authentication callback handler for the given configuration.
     *
     * @param configuration the configuration
     * @return the callback handler
     */
    public CallbackHandler getCallbackHandler(AuthenticationConfiguration configuration) {
        return configuration.getCallbackHandler();
    }

    /**
     * Get the actual host to use for the given configuration and URI.
     *
     * @param uri the URI
     * @param configuration the configuration
     * @return the real host to use
     */
    public String getRealHost(URI uri, AuthenticationConfiguration configuration) {
        return configuration.getHost(uri);
    }

    /**
     * Get the actual port to use for the given configuration and URI.
     *
     * @param uri the URI
     * @param configuration the configuration
     * @return the real port to use
     */
    public int getRealPort(URI uri, AuthenticationConfiguration configuration) {
        return configuration.getPort(uri);
    }

    /**
     * Get the principal to use for the given configuration.
     *
     * @param configuration the configuration
     * @return the principal
     */
    public Principal getPrincipal(AuthenticationConfiguration configuration) {
        return configuration.getPrincipal();
    }

    /**
     * Create a SASL client using the given URI and configuration, and using the caller's class loader and any globally
     * installed providers for the purposes of discovering available SASL client implementations.
     *
     * @param uri the target URI
     * @param configuration the authentication configuration
     * @param offeredMechanisms the available mechanisms
     * @return the SASL client, or {@code null} if no clients were available or could be configured
     */
    public SaslClient createSaslClient(URI uri, AuthenticationConfiguration configuration, Collection<String> offeredMechanisms) {
        final Iterator<SaslClientFactory> iterator = SaslFactories.getSaslClientFactories(getClassLoaderPrivileged(STACK_INSPECTOR.getCallerClass(1)), true);
        return configuration.createSaslClient(uri, null, null, iterator, offeredMechanisms);
    }

    /**
     * Create a SASL client using the given URI and configuration, using the given class loader and optionally any globally
     * installed providers for the purposes of discovering available SASL client implementations.
     *
     * @param uri the target URI
     * @param configuration the authentication configuration
     * @param offeredMechanisms the available mechanisms
     * @param classLoader the class loader to search
     * @param useGlobal {@code true} to fall back to global providers, {@code false} to only use providers from the class loader
     * @return the SASL client, or {@code null} if no clients were available or could be configured
     */
    public SaslClient createSaslClient(URI uri, AuthenticationConfiguration configuration, Collection<String> offeredMechanisms, ClassLoader classLoader, boolean useGlobal) {
        final Iterator<SaslClientFactory> iterator = SaslFactories.getSaslClientFactories(classLoader, useGlobal);
        return configuration.createSaslClient(uri, null, null, iterator, offeredMechanisms);
    }

    /**
     * Create a SASL client using the given URI and configuration, using the given class loader and optionally any globally
     * installed providers for the purposes of discovering available SASL client implementations.
     *
     * @param uri the target URI
     * @param localAddress the local socket address
     * @param peerAddress the peer socket address
     * @param configuration the authentication configuration
     * @param offeredMechanisms the available mechanisms
     * @param classLoader the class loader to search
     * @param useGlobal {@code true} to fall back to global providers, {@code false} to only use providers from the class loader
     * @return the SASL client, or {@code null} if no clients were available or could be configured
     */
    public SaslClient createSaslClient(URI uri, SocketAddress localAddress, SocketAddress peerAddress, AuthenticationConfiguration configuration, Collection<String> offeredMechanisms, ClassLoader classLoader, boolean useGlobal) {
        final Iterator<SaslClientFactory> iterator = SaslFactories.getSaslClientFactories(classLoader, useGlobal);
        return configuration.createSaslClient(uri, localAddress, peerAddress, iterator, offeredMechanisms);
    }


}
