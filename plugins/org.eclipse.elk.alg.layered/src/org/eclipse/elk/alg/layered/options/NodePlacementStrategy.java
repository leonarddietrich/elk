/*******************************************************************************
 * Copyright (c) 2012, 2015 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.layered.options;

import org.eclipse.elk.alg.layered.LayeredPhases;
import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.p4nodes.InteractiveNodePlacer;
import org.eclipse.elk.alg.layered.p4nodes.LinearSegmentsNodePlacer;
import org.eclipse.elk.alg.layered.p4nodes.NetworkSimplexPlacer;
import org.eclipse.elk.alg.layered.p4nodes.SimpleNodePlacer;
import org.eclipse.elk.alg.layered.p4nodes.SwimlanePlacer;
import org.eclipse.elk.alg.layered.p4nodes.TreelikeCenter;
import org.eclipse.elk.alg.layered.p4nodes.bk.BKNodePlacer;
import org.eclipse.elk.core.alg.ILayoutPhase;
import org.eclipse.elk.core.alg.ILayoutPhaseFactory;
import org.eclipse.elk.graph.properties.AdvancedPropertyValue;

/**
 * Definition of the available node placement strategies for the layered layout approach.
 */
public enum NodePlacementStrategy implements ILayoutPhaseFactory<LayeredPhases, LGraph> {

    /**
     * Very simple and very fast node placement that centers all nodes vertically.
     */
    SIMPLE,

    /**
     * Interactive node placer that leaves y coordinates of nodes untouched.
     */
    @AdvancedPropertyValue
    INTERACTIVE,

    /**
     * Node placement algorithm that aligns long edges using linear segments. Nodes are aligned according to the
     * <em>pendulum</em> method, which is similar to the barycenter method for node ordering.
     */
    LINEAR_SEGMENTS,

    /**
     * Node placement which groups nodes to blocks which result in straight edges.
     */
    BRANDES_KOEPF,

    /**
     * Using an auxiliary graph and the {@link org.eclipse.elk.alg.layered.networksimplex.NetworkSimplex NetworkSimplex}
     * algorithm to calculate a balanced placement with straight long edges.
     */
    NETWORK_SIMPLEX,

    /**
     * Nodes are placed centered on lanes which are defined by the node option 'nodePlacement.swimlane.lane'. Lanes are
     * orthogonal to the layers. The default crossing minimization most like wont produce good results together with
     * this node placer. For that reason use the graph option
     * 'crossingMinimization.barycenterHeuristicStrategy: SWIMLANE_ORDER ' for a specialized crossing minimization
     * ({@link org.eclipse.elk.alg.layered.p3order.SwimlaneBarycenterHeuristic SwimlaneBarycenterHeuristic}).
     */
    SWIMLANE,

    /**
     * This node placer builds subtrees from the graph and centers parent nodes over its child nodes.
     */
    TREELIKE_CENTER;

    @Override
    public ILayoutPhase<LayeredPhases, LGraph> create() {
        switch (this) {
        case SIMPLE:
            return new SimpleNodePlacer();

        case INTERACTIVE:
            return new InteractiveNodePlacer();

        case LINEAR_SEGMENTS:
            return new LinearSegmentsNodePlacer();

        case BRANDES_KOEPF:
            return new BKNodePlacer();

        case NETWORK_SIMPLEX:
            return new NetworkSimplexPlacer();

        case SWIMLANE:
            return new SwimlanePlacer();

        case TREELIKE_CENTER:
            return new TreelikeCenter();

        default:
            throw new IllegalArgumentException("No implementation is available for the node placer " + this.toString());
        }
    }

}
