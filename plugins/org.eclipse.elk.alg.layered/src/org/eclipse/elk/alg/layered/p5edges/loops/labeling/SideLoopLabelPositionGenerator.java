/*******************************************************************************
 * Copyright (c) 2018 Kiel University and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.elk.alg.layered.p5edges.loops.labeling;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.elk.alg.layered.p5edges.loops.SelfLoopComponent;
import org.eclipse.elk.alg.layered.p5edges.loops.SelfLoopLabel;
import org.eclipse.elk.alg.layered.p5edges.loops.SelfLoopPort;
import org.eclipse.elk.alg.layered.p5edges.splines.SplinesMath;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.options.PortSide;

/**
 * Generates self loop label positions for opposing-side self loops.
 */
public class SideLoopLabelPositionGenerator extends AbstractSelfLoopLabelPositionGenerator {

    /**
     */
    public List<SelfLoopLabelPosition> generatePositions(final SelfLoopComponent component) {
        List<SelfLoopLabelPosition> positions = new ArrayList<>();
        
        List<SelfLoopPort> ports = component.getPorts();
        SelfLoopPort startPort = ports.get(0);
        SelfLoopPort endPort = ports.get(ports.size() - 1);

        KVector startPosition = startPort.getLPort().getPosition().clone().add(startPort.getLPort().getAnchor());
        KVector endPosition = endPort.getLPort().getPosition().clone().add(endPort.getLPort().getAnchor());

        double directionStart = SplinesMath.portSideToDirection(startPort.getPortSide());
        KVector dirVectorStart = new KVector(directionStart);
        
        double directionEnd = SplinesMath.portSideToDirection(endPort.getPortSide());
        KVector dirVectorEnd = new KVector(directionEnd);
        
        KVector firstBendpoint = startPosition.clone().add(
                dirVectorStart.clone().scale((startPort.getMaximumLevel() * EDGE_DISTANCE) + LABEL_SPACING));

        KVector secondBendpoint = endPosition.clone().add(
                dirVectorEnd.clone().scale((endPort.getMaximumLevel() * EDGE_DISTANCE) + LABEL_SPACING));

        // Centered Positions
        List<SelfLoopLabelPosition> centeredPositions = createPositions(
                component.getLabel(), firstBendpoint, secondBendpoint, startPort, endPort);
        positions.addAll(centeredPositions);

        return positions;
    }

    private List<SelfLoopLabelPosition> createPositions(final SelfLoopLabel label, final KVector firstBendpoint,
            final KVector secondBendpoint, final SelfLoopPort startPort, final SelfLoopPort endPort) {
        
        List<SelfLoopLabelPosition> positions = new ArrayList<>();

        KVector startPosition = startPort.getLPort().getPosition().clone().add(startPort.getLPort().getAnchor());
        KVector endPosition = endPort.getLPort().getPosition().clone().add(endPort.getLPort().getAnchor());

        SelfLoopLabelPosition firstShortSegment = shortSegmentCenteredPosition(
                label, firstBendpoint, secondBendpoint, startPort);
        firstShortSegment.setPenalty(SelfLoopLabelPenalties.getSidePenalty(startPort.getPortSide()));
        positions.add(firstShortSegment);

        SelfLoopLabelPosition leftCenteredSegment = sideCentered(
                startPort, startPosition, firstBendpoint, label, true);
        double penalty = leftCenteredSegment.getPenalty() + SelfLoopLabelPenalties.SHORT_SEGMENT;
        leftCenteredSegment.setPenalty(penalty);
        positions.add(leftCenteredSegment);

        SelfLoopLabelPosition rightCenteredSegment = sideCentered(
                endPort, endPosition, secondBendpoint, label, false);
        penalty = rightCenteredSegment.getPenalty() + SelfLoopLabelPenalties.SHORT_SEGMENT;
        rightCenteredSegment.setPenalty(penalty);
        positions.add(rightCenteredSegment);

        // Side aligned positions
        SelfLoopLabelPosition firstShortSegmentRightBottomAligned = shortSegmentAlignedPosition(
                label, firstBendpoint, secondBendpoint, startPort, false);
        positions.add(firstShortSegmentRightBottomAligned);

        SelfLoopLabelPosition firstShortSegmentLeftTopAligned = shortSegmentAlignedPosition(
                label, firstBendpoint, secondBendpoint, startPort, true);
        positions.add(firstShortSegmentLeftTopAligned);

        // Side positions
        SelfLoopLabelPosition leftsideSegmentRightBottomAligned = sideCentered(
                startPort, startPosition, firstBendpoint, label, false, true);
        penalty = leftsideSegmentRightBottomAligned.getPenalty() + SelfLoopLabelPenalties.SHORT_SEGMENT;
        leftsideSegmentRightBottomAligned.setPenalty(penalty);
        positions.add(leftsideSegmentRightBottomAligned);

        SelfLoopLabelPosition leftsideSegmentLeftTopAligned = sideCentered(
                startPort, startPosition, firstBendpoint, label, true, true);
        penalty = leftsideSegmentRightBottomAligned.getPenalty() + SelfLoopLabelPenalties.SHORT_SEGMENT;
        leftsideSegmentRightBottomAligned.setPenalty(penalty);
        positions.add(leftsideSegmentLeftTopAligned);

        SelfLoopLabelPosition rightsideSegmentRightBottomAligned = sideCentered(
                endPort, endPosition, secondBendpoint, label, false, false);
        penalty = rightsideSegmentRightBottomAligned.getPenalty() + SelfLoopLabelPenalties.SHORT_SEGMENT;
        rightsideSegmentRightBottomAligned.setPenalty(penalty);
        positions.add(rightsideSegmentRightBottomAligned);

        SelfLoopLabelPosition rightsideSegmentLeftTopAligned = sideCentered(
                endPort, endPosition, secondBendpoint, label, true, false);
        penalty = rightsideSegmentLeftTopAligned.getPenalty() + SelfLoopLabelPenalties.SHORT_SEGMENT;
        rightsideSegmentLeftTopAligned.setPenalty(penalty);
        positions.add(rightsideSegmentLeftTopAligned);

        return positions;

    }

    private SelfLoopLabelPosition sideCentered(final SelfLoopPort closestPort, final KVector pointClosestToPort,
            final KVector endPoint, final SelfLoopLabel alabel, final boolean left) {
        
        PortSide startSide = closestPort.getPortSide();
        PortSide labelSideCenter = left ? startSide.left() : startSide.right();

        KVector startPortPoint = null;
        KVector endPortPoint = null;

        switch (startSide) {
        case NORTH:
        case WEST:
            startPortPoint = endPoint;
            endPortPoint = pointClosestToPort;
            break;
        case SOUTH:
        case EAST:
            startPortPoint = pointClosestToPort;
            endPortPoint = endPoint;
            break;
        }

        double penalty = SelfLoopLabelPenalties.getSidePenalty(labelSideCenter);

        KVector topAlignRightSide = null;
        topAlignRightSide = sideCenteredCoordinates(startPortPoint, endPortPoint, alabel, labelSideCenter);

        SelfLoopLabelPosition centerTopPosition = new SelfLoopLabelPosition(alabel, topAlignRightSide);
        centerTopPosition.setSide(startSide);
        centerTopPosition.setPenalty(penalty);

        return centerTopPosition;
    }

    private SelfLoopLabelPosition sideCentered(final SelfLoopPort closestPort, final KVector pointClosestToPort,
            final KVector endPoint, final SelfLoopLabel alabel, final boolean leftTopAligned, final boolean left) {
        
        PortSide startSide = closestPort.getPortSide();
        PortSide labelSideCenter = left ? startSide.left() : startSide.right();

        KVector startPortPoint = null;
        KVector endPortPoint = null;

        switch (startSide) {
        case NORTH:
        case WEST:
            startPortPoint = endPoint;
            endPortPoint = pointClosestToPort;
            break;
        case SOUTH:
        case EAST:
            startPortPoint = pointClosestToPort;
            endPortPoint = endPoint;
            break;
        }

        double penalty = SelfLoopLabelPenalties.getSidePenalty(labelSideCenter) + SelfLoopLabelPenalties.SHORT_SEGMENT;

        KVector topAlignRightSide = null;
        if (!leftTopAligned) {
            topAlignRightSide = bottomRightAlignedCoordinates(alabel, startPortPoint, endPortPoint, labelSideCenter);
            penalty += SelfLoopLabelPenalties.RIGHT_BOTTOM_ALIGNED;
        } else {
            topAlignRightSide = topLeftAlignedCoordinates(alabel, startPortPoint, endPortPoint, labelSideCenter);
            penalty += SelfLoopLabelPenalties.LEFT_TOP_ALIGNED;
        }

        SelfLoopLabelPosition centerTopPosition = new SelfLoopLabelPosition(alabel, topAlignRightSide);
        centerTopPosition.setSide(startSide);
        centerTopPosition.setPenalty(penalty);

        return centerTopPosition;
    }
}