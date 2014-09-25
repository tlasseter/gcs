package com.Sts.PlugIns.Model.DBTypes;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Surfaces.DBTypes.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

/**
 * Copyright:  Copyright (c) 2012
 * Author: Tom Lasseter
 * Date: 8/2/12
 */
public class StsLineSections extends StsObject
{
	/** Line on or connected to sections */
	protected StsLine line;
	protected StsSection onSection = null;
	/** Sections connected to this line */
	protected StsObjectRefList connectedSections = null;
	/** For a faultSection: RIGHT or LEFT */
	protected int sectionSide = 0;
	/** StsSurfaceVertex at intersection of well, surface, block: rotated coor system */
	protected StsObjectRefList sectionEdgeVertices = null;

	public transient boolean sectionInitialized = false;
	transient StsObjectList surfaceEdgeVertices = null;
	transient protected StsList zones = null;

	public static final int RIGHT = StsParameters.RIGHT;
	public static final int LEFT = StsParameters.LEFT;
	static public final int NONE = StsParameters.NONE;
	static public final int MINUS = StsParameters.MINUS;
	static public final int PLUS = StsParameters.PLUS;

	static final float nullValue = StsParameters.nullValue;

	public StsLineSections(StsLine line)
	{
		this.line = line;
		initializeVertices();
	}

	public void initializeVertices()
	{
		sectionEdgeVertices = StsObjectRefList.constructor(2, 2, "sectionEdgeVertices", this);
	}

	static public StsLineSections getLineSections(StsLine line)
	{
		return (StsLineSections)line.getLineSections();
	}

	static public boolean initializeSection(StsLine line)
	{
		return getLineSections(line).initializeSection();
	}
	public boolean initializeSection()
	{
		if (onSection == null) return true;
		if (!onSection.initialized) return false;
		if (sectionInitialized) return true;
		return sectionInitialized = projectToSection();
	}

	static public boolean reinitialize(StsLine line)
	{
		return getLineSections(line).reinitialize();
	}
	public boolean reinitialize()
	{
		try
		{
			if (onSection == null) return true;
			projectToSection();
			return sectionInitialized = onSection.initialized && !sectionInitialized;
		}
		catch (Exception e)
		{
			StsException.outputWarningException(this, "reinitialize", e);
			return false;
		}
	}

	public boolean addConnectedSection(StsSection section)
	{
		int nTotalSections = 0;
		if (this.onSection != null)
			nTotalSections = 1;
		if (connectedSections == null)
			connectedSections = StsObjectRefList.constructor(2, 2, "connectedSections", this);
		else
		{
			int nConnectedSections = connectedSections.getSize();
			nTotalSections += nConnectedSections;
		}
		if (nTotalSections >= 2)
		{
			StsMessageFiles.errorMessage("Two sections already connected at this StsLine: " + toSectionString() + ". Cannot add another.");
			return false;
		}
		return connectedSections.add(section);
	}

	public String toSectionString()
	{
		String sectionString = "none";
		if (onSection != null)
			sectionString = onSection.toString();
		return getName() + " section: " + sectionString;
	}

	public void deleteSection(StsSection section)
	{
		if (this.onSection == section)
		{
			section.deleteLine(line);
			this.onSection = null;
			sectionSide = StsParameters.NONE;
		}
		else if (connectedSections != null)
		{
			deleteConnectedSection(section);
		}

		if (!hasConnectedSection() && this.onSection != null)
		{
			this.onSection.deleteLine(line); // if line on section, delete from section side
			this.onSection = null;
		}

		delete(); // conditionally delete if no connected sections and not on a section
	}

	public boolean deleteConnectedSection(StsSection section)
	{
		if (connectedSections == null)
		{
			return false;
		}
		return connectedSections.delete(section);
	}

	static public StsSection getOnlyConnectedSection(StsLine line)
	{
		return getLineSections(line).getOnlyConnectedSection();
	}

	private StsSection getOnlyConnectedSection()
	{
		if (connectedSections == null || connectedSections.getSize() > 1)
		{
			return null;
		}
		else
		{
			return (StsSection) connectedSections.getElement(0);
		}
	}

	static public boolean hasConnectedSection(StsLine line)
	{
		return getLineSections(line).hasConnectedSection();
	}

	private boolean hasConnectedSection()
	{
		if (connectedSections == null)
		{
			return false;
		}
		int nConnectedSections = connectedSections.getSize();
		for (int n = 0; n < nConnectedSections; n++)
		{
			StsSection connectedSection = (StsSection) connectedSections.getElement(n);
			if (connectedSection != null && connectedSection.getIndex() >= 0)
			{
				return true;
			}
		}
		return false;
	}

	static public boolean hasSection(StsLine line)
	{
		return StsLineSections.getLineSections(line).hasSection();
	}

	public boolean hasSection()
	{
		return onSection != null || (connectedSections != null && connectedSections.getSize() > 0);
	}

	static public StsSection getPrevSection(StsLine line)
	{
		return getLineSections(line).getPrevSection();
	}

	private StsSection getPrevSection()
	{
		if (connectedSections == null)
		{
			return null;
		}
		int nConnectedSections = connectedSections.getSize();
		for (int n = 0; n < nConnectedSections; n++)
		{
			StsSection section = (StsSection) connectedSections.getElement(n);
			if (section.getLastLine() == line)
			{
				return section;
			}
		}
		return null;
	}

	static public StsSection getOtherSection(StsLine line, StsSection section)
	{
		return getLineSections(line).getOtherSection(section);
	}

	public StsSection getOtherSection(StsSection section)
	{
		if (connectedSections == null)
		{
			return null;
		}
		int nConnectedSections = connectedSections.getSize();
		if (nConnectedSections < 2)
		{
			return null;
		}
		for (int n = 0; n < nConnectedSections; n++)
		{
			StsSection otherSection = (StsSection) connectedSections.getElement(n);
			if (section != otherSection)
			{
				return otherSection;
			}
		}
		return null;
	}

	public StsSection getOnSection()
	{
		return onSection;
	}

	public int getSectionSide()
	{
		return sectionSide;
	}

	public StsObjectRefList getSectionEdgeVertices()
	{
		return sectionEdgeVertices;
	}

	static public StsSection[] getAllSections(StsLine line)
	{
		return getLineSections(line).getAllSections();
	}

	private StsSection[] getAllSections()
	{
		int nTotalSections = 0;
		if(onSection != null)
			nTotalSections = 1;
		int nConnectedSections = 0;
		if(connectedSections != null)
		{
			nConnectedSections = connectedSections.getSize();
			nTotalSections += nConnectedSections;
		}
		StsSection[] sections = new StsSection[nTotalSections];
		int n = 0;
		if(onSection != null)
			sections[n++] = onSection;
		if(connectedSections == null) return sections;
		for(int i = 0; i < nConnectedSections; i++)
			sections[n++] = (StsSection)connectedSections.getElement(i);
		return sections;
	}

	static public StsSection[] getConnectedSections(StsLine line)
	{
		return getLineSections(line).getConnectedSections();
	}

	private StsSection[] getConnectedSections()
	{
		if(connectedSections == null)
			return new StsSection[0];
		else
			return (StsSection[]) connectedSections.getCastList(StsSection.class);
	}

	static public void setOnSection(StsLine line, StsSection onSection)
	{
		getLineSections(line).setOnSection(onSection);
	}

	private void setOnSection(StsSection onSection)
	{
		this.onSection = onSection;
		// reinitialize=true: on db reload reinitialize this as section has changed
		dbFieldChanged("onSection", onSection, true);
	}

	static public boolean addOnSection(StsLine line, StsSection section)
	{
		return getLineSections(line).addOnSection(section);
	}

	private boolean addOnSection(StsSection section)
	{
		try
		{
			setOnSection(section);
			section.setLineOnSide(line);
			return projectToSection();
		}
		catch (Exception e)
		{
			StsException.outputException("StsLine.addOnSection() failed.",
					e, StsException.WARNING);
			return false;
		}
	}

	static public boolean projectToSection(StsLine line)
	{
		return getLineSections(line).projectToSection();
	}

	public boolean projectToSection()
	{
		return line.getLineVectorSet().projectToSection(onSection);
	}

	public boolean delete()
	{
		if (hasConnectedSection()) return false;
		StsObjectRefList.deleteAll(sectionEdgeVertices);
		return true;
	}

	static public boolean deleteTransientZones(StsLine line)
	{
		return getLineSections(line).deleteTransientZones();
	}
	public boolean deleteTransientZones()
	{
		if(zones == null) return false;
		zones = null;
		return true;
	}

	static public int getNConnectedSections(StsLine line)
	{
		return getLineSections(line).getNConnectedSections();
	}

	private int getNConnectedSections()
	{
		if (connectedSections == null) return 0;
		return connectedSections.getSize();
	}

	static public boolean isFullyConnected(StsLine line)
	{
		return getLineSections(line).isFullyConnected();
	}
	// there can be a maximum of two sections connections to this well/fault
	// or one connected section if well is on a section
	public boolean isFullyConnected()
	{
		int nConnectedSections = getNConnectedSections();
		if (onSection != null)
		{
			if (nConnectedSections == 1) return true;
			StsException.systemError(this, "isFullyConnected", "Line " + toSectionString() +
					" not properly connected. Is on a section and connected to " + nConnectedSections + " other sections.");
			return false;
		}
		return nConnectedSections == 2;
	}

	static public boolean isFullyConnected(StsLine line, StsXYSurfaceGridable surface)
	{
		return getLineSections(line).isFullyConnected(surface);
	}

	public boolean isFullyConnected(StsXYSurfaceGridable surface)
	{
		if (connectedSections == null)
		{
			return false;
		}
		int nConnectedSections = connectedSections.getSize();
		if (nConnectedSections == 0)
		{
			return false;
		}
		for (int n = 0; n < nConnectedSections; n++)
		{
			StsSection section = (StsSection) connectedSections.getElement(n);
			if (!section.hasSurface(surface))
			{
				return false;
			}
		}
		return true;
	}

	static public int getMaxConnections(StsLine line)
	{
		return getLineSections(line).getMaxConnections();
	}

	private int getMaxConnections()
	{
		if (onSection != null)
		{
			return 1;
		}
		else
		{
			return 2;
		}
	}

	static public StsPoint getPointAtZ(StsLine line, float z, StsSection section, boolean extrapolate)
	{
		return getLineSections(line).getPointAtZ(z, section, extrapolate);
	}

	private StsPoint getPointAtZ(float z, StsSection section, boolean extrapolate)
	{
		StsPoint point = line.getXYZPointAtZorT(z, extrapolate);
		if (onSection == section) return point;
		else
		{
			float indexF = section.getLineIndexF(line);
			float vv[] = new float[]{point.getX(), point.getY(), point.getZorT(), indexF};
			return new StsPoint(vv);
		}
	}

	static public void setSectionSide(StsLine line, int side)
	{
		getLineSections(line).setSectionSide(side);
	}

	public void setSectionSide(int side)
	{
		sectionSide = side;
		dbFieldChanged("sectionSide", side);
	}

	static public void deleteSectionVertex(StsLine line, StsSurfaceVertex vertex)
	{
		getLineSections(line).deleteSectionVertex(vertex);
	}

	/** Remove a surfaceVertex from the appropriate vertices list */
	public void deleteSectionVertex(StsSurfaceVertex vertex)
	{
		if (sectionEdgeVertices == null)
		{
			return;
		}
		sectionEdgeVertices.delete(vertex);
	}

	static public StsSurfaceVertex getSectionEdgeVertex(StsLine line, StsXYSurfaceGridable surface)
	{
		return getLineSections(line).getSectionEdgeVertex(surface);
	}

	public StsSurfaceVertex getSectionEdgeVertex(StsXYSurfaceGridable surface)
	{
		StsSurfaceVertex vertex = null;
		try
		{
			if(sectionEdgeVertices != null)
				vertex = getEdgeVertex(sectionEdgeVertices.getList(), surface);
			if (vertex != null) return vertex;
			vertex = computeSectionEdgeSurfaceVertex(surface);
			insertSectionEdgeVertex(vertex);
			return vertex;
		}
		catch (Exception e)
		{
			StsException.outputException("StsLine.getSurfaceVertex() failed.",
					e, StsException.WARNING);
			return null;
		}
	}

	public StsSurfaceVertex getEdgeVertex(StsObjectList edgeVertices, StsXYSurfaceGridable surface)
	{
		if (edgeVertices == null) return null;
		int nVertices = edgeVertices.getSize();
		for (int n = 0; n < nVertices; n++)
		{
			StsSurfaceVertex vertex = (StsSurfaceVertex) edgeVertices.getElement(n);
			if (vertex.getSurface() == surface)
			{
				return vertex;
			}
		}
		return null;
	}

	public boolean insertSectionEdgeVertex(StsSurfaceVertex vertex)
	{
		if(sectionEdgeVertices == null)
			sectionEdgeVertices = StsObjectRefList.constructor(2, 2, "sectionEdgeVertices", this);
		return insertVertex(vertex, sectionEdgeVertices.getList());
	}

	public StsSurfaceVertex computeSectionEdgeSurfaceVertex(StsXYSurfaceGridable surface)
	{
		return computeSurfaceVertex(surface, true);
	}

	public StsSurfaceVertex computeSurfaceEdgeVertex(StsXYSurfaceGridable surface)
	{
		return computeSurfaceVertex(surface, false);
	}

	public StsSurfaceVertex computeSurfaceVertex(StsXYSurfaceGridable surface, boolean persistent)
	{
		StsGridPoint gridPoint = computeGridIntersect(surface);
		if (gridPoint == null)
		{
			return null;
		}
		surface.interpolateBilinearZ(gridPoint, true, true);
		return new StsSurfaceVertex(gridPoint.getPoint(), line, surface, null, persistent);
	}
	/** Try an iterative top-down method to find well-grid intersection.  If this fails,
	 * go to a methodical top-down search.
	 */

	static public StsGridPoint computeGridIntersect(StsLine line, StsXYSurfaceGridable grid)
	{
		return getLineSections(line).computeGridIntersect(grid);
	}

	public StsGridPoint computeGridIntersect(StsXYSurfaceGridable grid)
	{
		if (line.getIsVertical())
		{
			return computeVerticalGridIntersect(grid);
		}
		else
		{
			return computeGridIntersect(grid, grid.getZMin());
		}
		//            return computeGridIntersect(grid, grid.getZMin(), grid.getZMax());
	}
	// from this startZ iterate to final intersection
	public StsGridPoint computeGridIntersect(StsXYSurfaceGridable grid, float startZ)
	{
		int indexAbove, indexBelow;
		boolean aboveOK, belowOK;
		StsGridPoint point;
		try
		{
			float[] ztFloats = line.getLineVectorSet().getZorTFloats();
			int nPoints = line.getLineVectorSet().getNValues();

			if (startZ < ztFloats[0])
			{
				indexAbove = -1;
			}
			else if (startZ > ztFloats[nPoints - 1])
			{
				indexAbove = nPoints - 1;
			}
			else
				indexAbove = StsMath.binarySearch(ztFloats, nPoints, startZ);

			indexBelow = indexAbove + 1;
			aboveOK = indexAbove >= 0;
			belowOK = indexBelow < nPoints;

			while (aboveOK || belowOK)
			{
				if (aboveOK)
				{
					point = computeGridIntervalIntersect(grid, indexAbove + 1);
					if (point != null)
					{
						return point;
					}
					indexAbove--;
					if (indexAbove < 0)
					{
						aboveOK = false;
					}
				}
				if (belowOK)
				{
					point = computeGridIntervalIntersect(grid, indexBelow);
					if (point != null)
					{
						return point;
					}
					indexBelow++;
					if (indexBelow >= nPoints)
					{
						belowOK = false;
					}
				}
			}
			// didn't find intersection along wellLine: try extrapolations above and below

			point = computeGridIntervalIntersect(grid, -1);
			if (point != null) return point;
			point = computeGridIntervalIntersect(grid, nPoints);
			if (point != null) return point;
			StsException.systemError("StsLine.computeGridIntersect() failed." +
					" Fault: " + getLabel() + " Grid: " + grid.getLabel());
			return null;
		}
		catch (Exception e)
		{
			StsException.outputException("StsLine.computeGridIntersect() failed." +
					" Fault: " + getLabel() + " Grid: " + grid.getLabel(),
					e, StsException.WARNING);
			return null;
		}
	}

	public StsGridPoint computeVerticalGridIntersect(StsXYSurfaceGridable grid)
	{
		StsPoint point = line.getTopPoint().copy();
		StsGridPoint gridPoint = new StsGridPoint(point, grid);
		float z = grid.interpolateBilinearZ(gridPoint, true, true);
		if (z == nullValue)
		{
			StsException.systemError("StsLine.computeVerticalGridIntersect() failed." +
					" For well: " + getLabel());
			return null;
		}
		return gridPoint;
	}

	private StsGridPoint computeGridIntervalIntersect(StsXYSurfaceGridable grid, int indexBelow)
	{
		StsPoint point0, point1;
		StsPoint[] points = line.getAsCoorPoints();
		int nPoints = points.length;
		if (indexBelow <= 0)
		{
			point0 = line.getLineVectorSet().getXYZorTPoint(grid.getZMin(), true, isDepth);
			point1 = points[0];
		}
		else if (indexBelow > nPoints - 1)
		{
			point0 = points[nPoints - 1];
			point1 = line.getLineVectorSet().getXYZorTPoint(grid.getZMax(), true, isDepth);
		}
		else
		{
			point0 = points[indexBelow - 1];
			point1 = points[indexBelow];
		}

		StsGridCrossings gridCrossings = new StsGridCrossings(point0, point1, grid);
		return gridCrossings.getGridIntersection(grid);
	}

	static public StsSurfaceVertex getConstructSurfaceEdgeVertex(StsLine line, StsSurface surface, StsBlock block)
	{
		return getLineSections(line).getConstructSurfaceEdgeVertex(surface, block);
	}

	public StsSurfaceVertex getConstructSurfaceEdgeVertex(StsSurface surface, StsBlock block)
	{
		StsSurfaceVertex vertex;

		vertex = getSurfaceBlockVertex(surface, block);
		if (vertex != null) return vertex;

		// if not, copy point from an existing sectionLineVertex
		StsPoint sectionVertexPoint;
		StsSurfaceVertex sectionVertex = getSurfaceEdgeVertex(surface);
		if (sectionVertex != null)
			sectionVertexPoint = sectionVertex.getPoint();
		else
		{
			StsGridPoint gridPoint = computeGridIntersect(surface);
			if (gridPoint == null)
			{
				StsException.systemError("StsLine.construtInitialEdgeVertex() failed for " + getName() + " intersecting surface " + surface.getName());
				gridPoint = computeGridIntersect(surface);
				return null;
			}
			sectionVertexPoint = gridPoint.getPoint();
		}
//         point = sectionVertexPoint.getXYZorTPoint();
		vertex = new StsSurfaceVertex(sectionVertexPoint, line, surface, block, false);
		addSurfaceEdgeVertex(vertex);
		return vertex;
	}

	public void addSurfaceEdgeVertex(StsSurfaceVertex vertex)
	{
		if(surfaceEdgeVertices == null) surfaceEdgeVertices = new StsObjectList(2, 2);
		surfaceEdgeVertices.add(vertex);
	}

	private StsSurfaceVertex getSurfaceBlockVertex(StsSurface surface, StsBlock block)
	{
		if (surfaceEdgeVertices == null) return null;
		int nVertices = surfaceEdgeVertices.getSize();
		for (int n = 0; n < nVertices; n++)
		{
			StsSurfaceVertex vertex = (StsSurfaceVertex) surfaceEdgeVertices.getElement(n);
			if (vertex.getSurface() == surface && vertex.getBlock() == block) return vertex;
		}
		return null;
	}

	static public String lineOnSectionLabel(StsLine line)
	{
		return getLineSections(line).lineOnSectionLabel();
	}

	public String lineOnSectionLabel()
	{
		if (onSection == null)
		{
			return getLabel();
		}
		else
		{
			return new String(getLabel() + onSection.getLabel() + " " +
					StsParameters.sideLabel(sectionSide) + " ");
		}
	}

	public int getLineSectionEnd()
	{
		int nConnectedSections = connectedSections.getSize();
		if (nConnectedSections <= 0)
		{
			return NONE;
		}
		StsSection firstConnectedSection = (StsSection) connectedSections.getElement(0);
		if (firstConnectedSection == null)
		{
			return NONE;
		}
		return firstConnectedSection.getSidePosition(line);
	}

	static public boolean isDyingFault(StsLine line)
	{
		return getLineSections(line).isDyingFault();
	}
	// If well is on an auxilarySection or connected to one: it is a dyingFault.
	public boolean isDyingFault()
	{
		if (onSection != null)
		{
			return onSection.isAuxiliary();
		}

		int nConnectedSections = connectedSections == null ? 0 : connectedSections.getSize();
		for (int n = 0; n < nConnectedSections; n++)
		{
			StsSection connectedSection = (StsSection) connectedSections.getElement(n);
			if (connectedSection.isAuxiliary())
			{
				return true;
			}
		}
		return false;
	}
	public StsSurfaceVertex getSurfaceEdgeVertex(StsXYSurfaceGridable surface)
	{
		StsSurfaceVertex vertex = null;
		try
		{
			if(surfaceEdgeVertices != null)
				vertex = getEdgeVertex(surfaceEdgeVertices, surface);
			if (vertex != null) return vertex;
			vertex = computeSurfaceEdgeVertex(surface);
			insertSurfaceEdgeVertex(vertex);
			return vertex;
		}
		catch (Exception e)
		{
			StsException.outputException("StsLine.getSurfaceVertex() failed.",
					e, StsException.WARNING);
			return null;
		}
	}

	static public boolean hasSectionEdgeVertex(StsLine line, StsSurface surface)
	{
		return getLineSections(line).hasSectionEdgeVertex(surface);
	}
	private boolean hasSectionEdgeVertex(StsSurface surface)
	{
		if (sectionEdgeVertices == null)
		{
			return false;
		}
		int nVertices = sectionEdgeVertices.getSize();
		for (int n = 0; n < nVertices; n++)
		{
			StsSurfaceVertex vertex = (StsSurfaceVertex) sectionEdgeVertices.getElement(n);
			if (vertex.getSurface() == surface)
			{
				return true;
			}
		}
		return false;
	}

	public boolean insertSurfaceEdgeVertex(StsSurfaceVertex vertex)
	{
		if(surfaceEdgeVertices == null)
			surfaceEdgeVertices = new StsObjectList(2, 2);
		return insertVertex(vertex, surfaceEdgeVertices);
	}

	static public StsSurfaceVertex getVertexBelow(StsLine line, StsSurfaceVertex vertex)
	{
		return getLineSections(line).getVertexBelow(vertex);
	}

	private StsSurfaceVertex getVertexBelow(StsSurfaceVertex vertex)
	{
		StsXYSurfaceGridable surface = vertex.getSurface();
		if (!(surface instanceof StsModelSurface))
		{
			return null;
		}
		StsModelSurface modelSurface = (StsModelSurface) surface;
		StsSurface surfaceBelow = modelSurface.getModelSurfaceBelow();
		if (surfaceBelow == null)
		{
			return null;
		}

		StsBlock block = vertex.getBlock();
		int nVertices = surfaceEdgeVertices.getSize();
		for (int n = 0; n < nVertices; n++)
		{
			vertex = (StsSurfaceVertex) surfaceEdgeVertices.getElement(n);
			if (vertex.getSurface() == surfaceBelow && vertex.getBlock() == block)
			{
				return vertex;
			}
		}
		return null;
	}

	public boolean insertVertex(StsSurfaceVertex vertex, StsObjectList surfaceVertices)
	{
		if (vertex == null) return false;
		if (surfaceVertices.contains(vertex))
			return false;

		StsPoint p = vertex.getPoint();
		if (p == null)
			return false;
		StsProject proj = currentModel.getProject();
		if (proj == null)
			return false;

		float z = p.getZorT();
		if (z < proj.getZorTMin() || z > proj.getZorTMax())
			return false;

		float lastZ = -StsParameters.largeFloat;
		if (z == lastZ)
			return false;

		vertex.setAssociation(this);

		StsSurfaceVertex lastVertex, nextVertex;
		float nextZ;

		int nSurfaceVertices = surfaceVertices.getSize();
		if (nSurfaceVertices == 0)
			surfaceVertices.add(vertex);
		else if (nSurfaceVertices == 1)
		{
			nextVertex = (StsSurfaceVertex) surfaceVertices.getElement(0);
			nextZ = nextVertex.getPoint().getZorT();
			if (z <= nextZ)
				surfaceVertices.insertBefore(0, vertex);
			else
				surfaceVertices.add(vertex);
		}
		else
		{
			nextVertex = (StsSurfaceVertex) surfaceVertices.getElement(0);
			nextZ = nextVertex.getPoint().getZorT();

			for (int n = 1; n < nSurfaceVertices; n++)
			{
				lastZ = nextZ;
				nextVertex = (StsSurfaceVertex) surfaceVertices.getElement(n);
				nextZ = nextVertex.getPoint().getZorT();
				if (z >= lastZ && z <= nextZ)
				{
					surfaceVertices.insertBefore(n, vertex);
					return true;
				}
			}
			surfaceVertices.add(vertex);
		}
		return true;
	}

	static public StsLineZone getLineZone(StsLine line, StsSurfaceVertex topVertex, StsSurfaceVertex botVertex,
								   StsSection onSection, int sectionSide, int direction)
	{
		return getLineSections(line).getLineZone(topVertex, botVertex, onSection, sectionSide, direction);
	}

	private StsLineZone getLineZone(StsSurfaceVertex topVertex, StsSurfaceVertex botVertex,
								   StsSection onSection, int sectionSide, int direction)
	{
		if (onSection == null)
		{
			return null;
		}

		// If this is a defining well for the section, return wellZone on sectionSide
		if (onSection.getFirstLine() == line || onSection.getLastLine() == line)
		{
			return getLineZone(topVertex, botVertex, onSection, sectionSide);
		}

		int position = getLineSectionEnd();
		if (position == NONE)
		{
			return null;
		}

		if (position == MINUS && direction == MINUS || position == PLUS && direction == PLUS)
		{
			return getLineZone(topVertex, botVertex, onSection, RIGHT);
		}
		else
		{
			return getLineZone(topVertex, botVertex, onSection, RIGHT);
		}
	}

	static public StsLineZone getLineZone(StsLine line, StsSurfaceVertex topVertex, StsSurfaceVertex botVertex, StsSection onSection, int sectionSide)
	{
		return getLineSections(line).getLineZone(topVertex, botVertex, onSection, sectionSide);
	}

	private StsLineZone getLineZone(StsSurfaceVertex topVertex, StsSurfaceVertex botVertex, StsSection onSection, int sectionSide)
	{
		StsLineZone zone;

		if (zones == null)
		{
			zones = new StsList(2, 2);
		}

		int nZones = zones.getSize();
		for (int n = 0; n < nZones; n++)
		{
			zone = (StsLineZone) zones.getElement(n);
			if (zone.getTop() == topVertex)
			{
				return zone;
			}
		}

		/** Couldn't find zone: make one and store it in zones */
		zone = new StsLineZone(currentModel, topVertex, botVertex, line);
		zones.add(zone);
		return zone;
	}

	static public void deleteSurfaceVertices(StsLine line)
	{
		getLineSections(line).deleteSurfaceVertices();
	}

	public void deleteSurfaceVertices()
	{
		surfaceEdgeVertices = null;
	}

	private void projectVerticesToSection(StsObjectList verticesList)
	{
		int nVertices = verticesList.getSize();
		for(int n = 0; n < nVertices; n++)
		{
			StsSurfaceVertex vertex = (StsSurfaceVertex)verticesList.getElement(n);
			StsPoint point = line.getLineVectorSet().getNearestPointOnLine(vertex.getPoint());
		}
	}

	static public void projectVerticesToSection(StsLine line)
	{
		getLineSections(line).projectVerticesToSection();
	}

	public void projectVerticesToSection()
	{
		line.getLineVectorSet().projectToSection(onSection);
		// using the projected line vertices, spline a new set of points and project each point to the section
		computeExtendedPointsProjectToSection(onSection);
		projectVerticesToSection(surfaceEdgeVertices);
	}

	static public boolean computeExtendedPointsProjectToSection(StsLine line, float zMin, float zMax)
	{
		return getLineSections(line).computeExtendedPointsProjectToSection(zMin, zMax);
	}
	public boolean computeExtendedPointsProjectToSection(float zMin, float zMax)
	{
		if (!line.computeExtendedPoints(zMin, zMax)) return false;
		return projectToSection();
	}

	public boolean computePointsProjectToSection()
	{
		if (!line.checkComputeRelativePoints()) return false;
		return projectToSection();
	}

	static public boolean computeExtendedPointsProjectToSection(StsLine line, StsSection section)
	{
		return getLineSections(line).computeExtendedPointsProjectToSection(section);
	}
	public boolean computeExtendedPointsProjectToSection(StsSection section)
	{
		if(section == null) return false;
		return computeExtendedPointsProjectToSection(section.sectionZMin,  section.sectionZMax);
	}

	static public boolean areAllSectionsVertical(StsLine line)
	{
		return getLineSections(line).areAllSectionsVertical();
	}
	public boolean areAllSectionsVertical()
	{
		if (connectedSections == null)
		{
			return false;
		}
		for (int n = 0; n < connectedSections.getSize(); n++)
		{
			StsSection connectedSection = (StsSection) connectedSections.getElement(n);
			if (!connectedSection.isVertical())
			{
				return false;
			}
		}
		return true;
	}

	static public float[] getDipDirectionVector(StsLine line, StsPoint linePoint, int sectionEnd)
	{
		return getLineSections(line).getDipDirectionVector(linePoint, sectionEnd);
	}

	public float[] getDipDirectionVector(StsPoint linePoint, int sectionEnd)
	{

		if (onSection == null) return null;
		StsPoint tangent = onSection.getTangentAtPoint(linePoint, sectionSide);
		if (sectionEnd == MINUS && sectionSide == RIGHT || sectionEnd == PLUS && sectionSide == LEFT)
			tangent.reverse();
		return tangent.v;
	}

	static public float getTopSurfaceIndexF(StsLine line)
	{
		return getLineSections(line).getTopSurfaceIndexF();
	}
	public float getTopSurfaceIndexF()
	{
		if(!hasSurfaceEdgeVertices()) return 0;
		StsSurfaceVertex vertex = (StsSurfaceVertex)surfaceEdgeVertices.getFirst();
		return line.getLineVectorSet().getIndexF(vertex.getPoint());
	}

	static public float getBotSurfaceIndexF(StsLine line)
	{
		return getLineSections(line).getBotSurfaceIndexF();
	}
	public float getBotSurfaceIndexF()
	{
		if(!hasSurfaceEdgeVertices()) return line.getNValues()-1;
		StsSurfaceVertex vertex = (StsSurfaceVertex)surfaceEdgeVertices.getLast();
		return line.getLineVectorSet().getIndexF(vertex.getPoint());
	}

	static public boolean hasSurfaceEdgeVertices(StsLine line)
	{
		return getLineSections(line).hasSurfaceEdgeVertices();
	}
	public boolean hasSurfaceEdgeVertices()
	{
		return surfaceEdgeVertices != null && surfaceEdgeVertices.getSize() > 0;
	}

	static public void checkConstructWellZones(StsWell well)
	{
		getLineSections(well).checkConstructWellZones();
	}

	public void checkConstructWellZones()
	{
		if (zones != null) return;
		StsClass modelZones = currentModel.getCreateStsClass(StsZone.class); // these are zones in top to bottom order
		int nZones = zones.getSize();
		zones = new StsList(2, 2);
		for (int z = 0; z < nZones; z++)
		{
			StsZone zone = (StsZone) modelZones.getElement(z);
			StsWellZone wellZone = constructStratZone(zone);
			if (wellZone != null)
			{
				zones.add(wellZone);
			}
		}
	}

	private StsWellZone constructStratZone(StsZone zone)
	{
		StsModelSurface topHorizon = zone.getTopModelSurface();
		String topMarkerName = topHorizon.getMarkerName();
		StsWell well = (StsWell)line;
		StsWellMarker topMarker = well.getMarker(topMarkerName, StsParameters.STRAT);
		if (topMarker == null)
			topMarker = StsWellMarker.constructor(topHorizon, well);
		if (topMarker == null) return null;

		StsModelSurface baseHorizon = zone.getBaseModelSurface();
		String baseMarkerName = baseHorizon.getMarkerName();
		StsWellMarker baseMarker = ((StsWell)line).getMarker(baseMarkerName, StsParameters.STRAT);
		if (baseMarker == null)
		{
			baseMarker = StsWellMarker.constructor(baseHorizon, well);
		}
		if (baseMarker == null) return null;

		return StsWellZone.constructor(well, StsParameters.STRAT, topMarkerName, topMarker, baseMarker, zone);
	}

	static public void addZone(StsWell well, StsWellZone zone)
	{
		getLineSections(well).addZone(zone);
	}
	public void addZone(StsWellZone zone)
	{
		if (zone.getZoneType() != StsWellZoneSet.STRAT)  return;
		if (zones == null) zones = new StsList(2, 2);
		zones.add(zone);
	}

	static public int getNZones(StsLine line)
	{
		return getLineSections(line).getNZones();

	}
	/* get an number of well zones (doesn't handle fault zones) */
	public int getNZones()
	{
		if (zones != null)
			return zones.getSize();
		else
			return 0;
	}

	static public StsList getZoneList(StsLine line)
	{
		return getLineSections(line).getZoneList();
	}
	/* get object ref list of strat zones */
	public StsList getZoneList()
	{
		if(zones != null) return zones;
		return zones = new StsList(2, 2);
	}

	static public void setZoneList(StsLine line, StsList zones)
	{
		getLineSections(line).setZoneList(zones);
	}
	/* get object ref list of strat zones */
	private void setZoneList(StsList zones)
	{
		this.zones = zones;
	}

	static public StsLineZone getZone(StsLine line, String name)
	{
		return getLineSections(line).getZone(name);
	}
	public StsLineZone getZone(String name)
	{
		if (name == null || zones == null) return null;
		int nZones = zones.getSize();
		for(int n = 0; n < nZones; n++)
		{
			StsLineZone zone = (StsLineZone)zones.getElement(n);
			if (name.equals(zone.getLabel()))
				return zone;
		}
		return null;
	}
}
