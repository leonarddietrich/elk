/*******************************************************************************
 * Copyright (c) 2019 Kiel University and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.elk.alg.layered.intermediate.loops;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import org.eclipse.elk.alg.layered.graph.LPort;

/**
 * A port which is an end point of at least one self loop.
 */
public class SelfLoopPort {
    
    /** Port represented by this instance. */
    private final LPort lPort;
    /** Whether the port was only incident to self loop edges. */
    private final boolean hadOnlySelfLoops;
    /** Whether the original {@link LPort} is currently hidden from its node. */
    private boolean isHidden = false;
    /** List of incoming self loops. */
    private final List<SelfLoopEdge> incomingSLEdges = new ArrayList<>();
    /** List of outgoing self loops. */
    private final List<SelfLoopEdge> outgoingSLEdges = new ArrayList<>();
    
    /**
     * Creates a new instance to represent the given port.
     */
    SelfLoopPort(final LPort lPort) {
        assert lPort.getConnectedEdges().iterator().hasNext();
        
        this.lPort = lPort;
        
        // Check if the port is only incident to self loops
        hadOnlySelfLoops = StreamSupport.stream(lPort.getConnectedEdges().spliterator(), false)
                .allMatch(edge -> edge.isSelfLoop());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Accessors
    
    /**
     * Returns the {@link LPort} this {@link SelfLoopPort} was created for.
     */
    public LPort getLPort() {
        return lPort;
    }
    
    /**
     * Returns whether the port represented by this instance was only incident to self loop edges. That may influence
     * the decision of whether or not to hide the port from its original node.
     */
    public boolean hadOnlySelfLoops() {
        return hadOnlySelfLoops;
    }
    
    /**
     * Returns whether the original {@link LPort} is currently hidden from its node.
     */
    public boolean isHidden() {
        return isHidden;
    }
    
    /**
     * Sets whether the original {@link LPort} is currently hidden from its node.
     */
    public void setHidden(final boolean hidden) {
        this.isHidden = hidden;
    }
    
    /**
     * Returns the list of incoming {@link SelfLoopEdge}s.
     */
    public List<SelfLoopEdge> getIncomingSLEdges() {
        return incomingSLEdges;
    }
    
    /**
     * Returns the list of outgoing {@link SelfLoopEdge}s.
     */
    public List<SelfLoopEdge> getOutgoingSLEdges() {
        return outgoingSLEdges;
    }

}
