package com.Sts.Framework.IO.Locks;

import com.Sts.Framework.IO.*;

import java.util.concurrent.locks.*;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 10/5/11
 */
public class StsAbstractFileLock extends ReentrantReadWriteLock implements Comparable<StsAbstractFileLock>
{
	StsAbstractFile file;

	static final boolean debug = true;

	public StsAbstractFileLock(StsAbstractFile file)
	{
		this.file = file;
	}

	public void lockReader()
	{
		readLock().lock();
		if(debug)System.out.println("lockReader for " + file.filename);
	}

	public void unlockReader()
	{
		readLock().unlock();
		if(debug)System.out.println("unlockReader for " + file.filename);
	}

	public void lockWriter()
	{
		writeLock().lock();
		if(debug)System.out.println("lockWriter for " + file.filename);
	}

	public void unlockWriter()
	{
		writeLock().unlock();
		if(debug)System.out.println("unlockWriter for " + file.filename);
	}

	public boolean tryLockReader()
	{
		if(debug)System.out.println("tryLockReader for " + file.filename);
		return readLock().tryLock();
	}

	public boolean tryLockWriter()
	{
		if(debug)System.out.println("tryLockWriter for " + file.filename);
		return writeLock().tryLock();
	}

	public int compareTo(StsAbstractFileLock other)
	{
		return file.getPath().compareTo(other.file.getPath());
	}
}
