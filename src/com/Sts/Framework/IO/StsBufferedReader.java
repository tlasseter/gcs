package com.Sts.Framework.IO;

import java.io.*;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 11/12/11
 */
public class StsBufferedReader
{
// This example uses character arrays instead of Strings.
// It doesn't use BufferedReader or BufferedFileReader, but does
// the buffering by itself so that it can avoid creating too many
// String objects.  For simplicity, it assumes that no line will be
// longer than 128 characters.

	FileReader fr;
	int nlines = 0;
	char buffer[] = new char[8192 + 1];
	int maxLineLength = 128;

	//assumes no line is longer than this
	char lineBuf[] = new char[maxLineLength];

	public StsBufferedReader(String filename)
	{
		try
		{
			fr = new FileReader(filename);

			int nChars = 0;
			int nextChar = 0;
			int startChar = 0;
			boolean eol = false;
			int lineLength = 0;
			char c = 0;
			int n;
			int j;

			while (true)
			{
				if(nextChar >= nChars)
				{
					n = fr.read(buffer, 0, 8192);
					if(n == -1)
					{  // EOF
						break;
					}
					nChars = n;
					startChar = 0;
					nextChar = 0;
				}

				for(j = nextChar; j < nChars; j++)
				{
					c = buffer[j];
					if((c == '\n') || (c == '\r'))
					{
						eol = true;
						break;
					}
				}
				nextChar = j;

				int len = nextChar - startChar;
				if(eol)
				{
					nextChar++;
					if((lineLength + len) > maxLineLength)
					{
						// error
					}
					else
					{
						System.arraycopy(buffer, startChar, lineBuf, lineLength, len);
					}
					lineLength += len;

					//
					// Process line here
					//
					nlines++;

					if(c == '\r')
					{
						if(nextChar >= nChars)
						{
							n = fr.read(buffer, 0, 8192);
							if(n != -1)
							{
								nextChar = 0;
								nChars = n;
							}
						}

						if((nextChar < nChars) && (buffer[nextChar] == '\n'))
							nextChar++;
					}
					startChar = nextChar;
					lineLength = 0;
					continue;
				}

				if((lineLength + len) > maxLineLength)
				{
					// error
				}
				else
				{
					System.arraycopy(buffer, startChar, lineBuf, lineLength, len);
				}
				lineLength += len;
			}
			fr.close();
		}
		catch(Exception e)
		{
			System.out.println("exception: " + e);
		}
	}
}
