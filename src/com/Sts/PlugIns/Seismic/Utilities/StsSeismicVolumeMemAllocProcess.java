package com.Sts.PlugIns.Seismic.Utilities;

import com.Sts.Framework.Types.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Seismic.DBTypes.*;

/** This class allocates 3 chunks of memory used for byte volume output: outputRowPlaneBytes, outputColBlockBytes, outputSliceBlockBytes
 *  The size of the first is fixed (nInputCols*nSamples), but the other two are sized by nRowsPerBlock*nInputCols*nSamples.
 *  Allocated a specified fraction of free memory, we allocate as many rowsPerBlock as can be accommodated.
 */
public class StsSeismicVolumeMemAllocProcess extends StsVolumeMemAllocProcess
{
    private StsSeismicVolumeMemAllocProcess(StsSeismicVolume inputVolume, StsSeismicVolume outputVolume) throws StsException
    {
        initializeMemoryAllocation();
        setupInputMemory(inputVolume);
        setupOutputMemory(outputVolume, false);
    }

    private StsSeismicVolumeMemAllocProcess(StsSeismicVolume outputVolume) throws StsException
    {
        initializeMemoryAllocation();
        setupInputMemory(outputVolume);
        setupOutputMemory(outputVolume, false);
    }

    static public StsSeismicVolumeMemAllocProcess constructor(StsSeismicVolume inputVolume, StsSeismicVolume outputVolume)
    {
        try
        {
            return new StsSeismicVolumeMemAllocProcess(inputVolume, outputVolume);
        }
 		catch(Exception e)
		{
			StsException.systemError(e.getMessage());
			return null;
		}
    }

    static public StsSeismicVolumeMemAllocProcess constructor(StsSeismicVolume outputVolume)
    {
        try
        {
            return new StsSeismicVolumeMemAllocProcess(outputVolume);
        }
 		catch(Exception e)
		{
			StsException.systemError(e.getMessage());
			return null;
		}
    }

	public void setupInputMemory(StsRotatedGridBoundingBox inputVolume)
	{
		StsSeismicVolume volume = (StsSeismicVolume)inputVolume;
		nInputRows = volume.nRows;
		nInputCols = volume.nCols;
		nInputSlices = volume.nSlices;
		if(volume.isDataFloat)
		{
			nBytesPerInputSample = 4;
			nInputBlocks = volume.fileMapRowFloatBlocks.nTotalBlocks;
			nInputRowsPerBlock = (int)volume.fileMapRowFloatBlocks.nPlanesPerBlock;
		}
		else
		{
			nBytesPerInputSample = 1;
			nInputBlocks = volume.filesMapBlocks[1].nTotalBlocks;
			nInputRowsPerBlock = (int)volume.filesMapBlocks[1].nPlanesPerBlock;
		}
		nInputBytesPerRow = nInputCols*nInputSlices*nBytesPerInputSample;
		nInputBlockTraces = nInputRowsPerBlock*nInputCols;
		nInputSamplesPerBlock = nInputBlockTraces*nInputSlices;
		nInputBytesPerBlock = nInputSamplesPerBlock*nBytesPerInputSample;
	}
}
