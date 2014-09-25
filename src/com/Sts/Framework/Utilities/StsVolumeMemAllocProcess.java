package com.Sts.Framework.Utilities;

import com.Sts.Framework.Types.*;

/** This class allocates 3 chunks of memory used for byte volume output: outputRowPlaneBytes, outputColBlockBytes, outputSliceBlockBytes
 *  The size of the first is fixed (nInputCols*nSamples), but the other two are sized by nRowsPerBlock*nInputCols*nSamples.
 *  Allocated a specified fraction of free memory, we allocate as many rowsPerBlock as can be accommodated.
 */
abstract public class StsVolumeMemAllocProcess extends StsMemoryAllocation
{
    public int nInputRows;
    public int nInputCols;
    public int nInputSlices;
    public int nOutputRows;
    public int nOutputCols;
    public int nOutputSlices;
    public int nBytesPerInputSample;
    public int nBytesPerOutputSample;
    public int nInputVolumes = 1;
    public long nInputBytesPerRow;
//    public int nOutputBytesPerRow;
    public int nInputRowsPerBlock;
    public int nOutputRowsPerBlock;
    public int nInputBlocks;
    public int nOutputBlocks;
    public int nInputBytesPerBlock;
//    public int nBlocksPerWrite;
    public int nOutputBytesPerBlock;
    public int nInputBlockTraces;
    public int nOutputSamplesPerRow;
    public int nInputSamplesPerBlock;
    public int nOutputSamplesPerInputBlock;
    public byte[] outputRowPlaneBytes;
	public byte[] outputColBlockBytes;
	public byte[] outputSliceBlockBytes;

	abstract public void setupInputMemory(StsRotatedGridBoundingBox inputVolume);

	public void setupOutputMemory(StsRotatedGridBoundingBox volume, boolean useOutputMappedBuffers) throws StsException
	{
		nOutputRows = volume.nRows;
		nOutputCols = volume.nCols;
		nOutputSlices = volume.nSlices;
		nBytesPerOutputSample = 4;

		nOutputSamplesPerRow = nOutputCols*nOutputSlices;
		// mapBuffer blockSize is controlled by the number of bytes in a float block we are processing
		// we will write out a rpo
		int nOutputBytesPerRow = nOutputSamplesPerRow*nBytesPerOutputSample;
		nOutputRowsPerBlock = Math.max(1, (int)(maxMemoryToUse-nOutputSamplesPerRow)/(2*nOutputSamplesPerRow));
		/*
				if(useOutputMappedBuffers)
					nOutputRowsPerBlock = Math.max(1, (int)(maxMapBufferSize/nOutputSamplesPerRow));
				else
					nOutputRowsPerBlock = Math.max(1, (int)(maxMemoryToUse-nOutputSamplesPerRow)/(2*nOutputSamplesPerRow));
			*/
		nOutputRowsPerBlock = Math.min(nOutputRows, nOutputRowsPerBlock);
		// not currently used
		nOutputBytesPerBlock = nOutputRowsPerBlock*nOutputSamplesPerRow;
		nOutputBlocks = nOutputRows/nOutputRowsPerBlock;
		if(nOutputRowsPerBlock*nOutputBlocks < nOutputRows) nOutputBlocks++;
		nOutputSamplesPerInputBlock = nInputRowsPerBlock*nOutputSamplesPerRow;
	}

	protected boolean nextIterationOK() { return nOutputRowsPerBlock > 1; }

    protected boolean allocateMemoryIteration()
	{
		int nTotalBytesPerOutputBlock = 0;
        try
		{
            outputRowPlaneBytes = new byte[nOutputSamplesPerRow];
            nOutputRowsPerBlock = (int)(maxMemoryToUse - nOutputSamplesPerRow)/(2* nOutputSamplesPerRow);
            nOutputRowsPerBlock = StsMath.minMax(nOutputRowsPerBlock, 1, nOutputRows);
            nOutputBytesPerBlock = nOutputRowsPerBlock * nOutputCols * nOutputSlices;
			outputColBlockBytes = new byte[nOutputBytesPerBlock];
			outputSliceBlockBytes = new byte[nOutputBytesPerBlock];
            nTotalBytesPerOutputBlock = nOutputSamplesPerRow + 2* nOutputBytesPerBlock;
            checkMemoryStatus("allocate memory succeeded for " + nOutputRowsPerBlock + " rowsPerBlock and " + nTotalBytesPerOutputBlock + " bytes");
		}
		catch (OutOfMemoryError e)
		{
			freeMemory();
			checkMemoryStatus("allocate memory failed for " + nOutputRowsPerBlock + " rowsPerBlock and " + nTotalBytesPerOutputBlock + " bytes");
			nOutputRowsPerBlock /= 2;
			return false;
		}
		return true;
	}

    public void freeMemory()
    {
        outputRowPlaneBytes = null;
        outputColBlockBytes = null;
	    outputColBlockBytes = null;
    }
}
