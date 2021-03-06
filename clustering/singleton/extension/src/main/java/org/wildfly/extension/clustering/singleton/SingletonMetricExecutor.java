/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.wildfly.extension.clustering.singleton;

import java.util.function.Function;
import java.util.function.Supplier;

import org.jboss.as.clustering.controller.Metric;
import org.jboss.as.clustering.controller.MetricExecutor;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceNotFoundException;
import org.jboss.msc.service.ServiceRegistry;
import org.wildfly.clustering.service.PassiveServiceSupplier;
import org.wildfly.clustering.singleton.Singleton;

/**
 * Generic executor for singleton metrics.
 * @author Paul Ferraro
 */
public class SingletonMetricExecutor implements MetricExecutor<Singleton> {

    private final Function<String, ServiceName> serviceNameFactory;

    public SingletonMetricExecutor(Function<String, ServiceName> serviceNameFactory) {
        this.serviceNameFactory = serviceNameFactory;
    }

    @Override
    public ModelNode execute(OperationContext context, Metric<Singleton> metric) throws OperationFailedException {
        ServiceName name = this.serviceNameFactory.apply(context.getCurrentAddressValue());
        Singleton singleton = findSingleton(context.getServiceRegistry(false), name);
        return metric.execute(singleton);
    }

    @SuppressWarnings("deprecation")
    private static Singleton findSingleton(ServiceRegistry registry, ServiceName name) {
        try {
            Supplier<Singleton> singleton = new PassiveServiceSupplier<>(registry, name.append("singleton"));
            return singleton.get();
        } catch (ServiceNotFoundException e) {
            // Legacy logic
            return (Singleton) registry.getRequiredService(name).getService();
        }
    }
}
