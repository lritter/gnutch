package org.apache.hadoop.extensions;

import java.io.IOException;

import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;

public class FileOnlyPathFilter implements PathFilter {

    protected FileSystem fs;

    public FileOnlyPathFilter(FileSystem _fs)
    {
	fs = _fs;
    }

    public boolean accept(Path path) {
	try
	{
	    return fs.isFile(path);
	}
	catch(IOException e)
	{
	    return false;
	}
    }
}