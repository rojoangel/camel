/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.model;

import java.util.List;
import java.util.concurrent.Executor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.camel.Processor;
import org.apache.camel.processor.MulticastProcessor;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.processor.aggregate.UseLatestAggregationStrategy;
import org.apache.camel.processor.interceptor.StreamCachingInterceptor;
import org.apache.camel.spi.RouteContext;

/**
 * Represents an XML &lt;multicast/&gt; element
 *
 * @version $Revision$
 */
@XmlRootElement(name = "multicast")
@XmlAccessorType(XmlAccessType.FIELD)
public class MulticastDefinition extends OutputDefinition<ProcessorDefinition> {
    @XmlAttribute(required = false)
    private Boolean parallelProcessing;
    @XmlAttribute(required = false)
    private String strategyRef;
    @XmlAttribute(required = false)
    private String threadPoolRef;    
    @XmlTransient
    private AggregationStrategy aggregationStrategy;
    @XmlTransient
    private Executor executor;

    @Override
    public String toString() {
        return "Multicast[" + getOutputs() + "]";
    }

    @Override
    public String getShortName() {
        return "multicast";
    }

    @Override
    public Processor createProcessor(RouteContext routeContext) throws Exception {
        return createOutputsProcessor(routeContext);
    }

    // Fluent API
    // -------------------------------------------------------------------------

    /**
     * Set the multicasting aggregationStrategy
     *
     * @return the builder
     */
    public MulticastDefinition aggregationStrategy(AggregationStrategy aggregationStrategy) {
        setAggregationStrategy(aggregationStrategy);
        return this;
    }
    
    /**
     * use a thread pool to do the multicasting work
     *     
     * @return the builder
     */
    public MulticastDefinition parallelProcessing() {
        setParallelProcessing(true);
        return this;
    }
    
    /**
     * Set the multicasting action's thread model
     *
     * @param parallelProcessing <tt>true</tt> to use a thread pool, if <tt>false</tt> then work is done in the
     * calling thread.
     *
     * @return the builder
     */
    public MulticastDefinition parallelProcessing(boolean parallelProcessing) {
        setParallelProcessing(parallelProcessing);
        return this;
    }
    
    /**
     * Setting the executor for executing the multicasting action. 
     *
     * @return the builder
     */
    public MulticastDefinition executor(Executor executor) {
        setExecutor(executor);
        return this;
    }    
        
    protected Processor createCompositeProcessor(RouteContext routeContext, List<Processor> list) {
        if (aggregationStrategy == null && strategyRef != null) {
            aggregationStrategy = routeContext.lookup(strategyRef, AggregationStrategy.class);
        }
        if (aggregationStrategy == null) {
            aggregationStrategy = new UseLatestAggregationStrategy();
        }
        if (threadPoolRef != null) {
            executor = routeContext.lookup(threadPoolRef, Executor.class);
        }
        return new MulticastProcessor(list, aggregationStrategy, isParallelProcessing(), executor);
    }

    public AggregationStrategy getAggregationStrategy() {
        return aggregationStrategy;
    }

    public MulticastDefinition setAggregationStrategy(AggregationStrategy aggregationStrategy) {
        this.aggregationStrategy = aggregationStrategy;
        return this;
    }

    public boolean isParallelProcessing() {
        return parallelProcessing != null ? parallelProcessing : false;
    }

    public void setParallelProcessing(boolean parallelProcessing) {
        this.parallelProcessing = parallelProcessing;        
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;        
    }
    
}