package org.apache.hadoop.extensions;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.*;

import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.fs.FileUtil;

import org.apache.hadoop.extensions.FileOnlyPathFilter;

public class FileOnlySequenceFileOutputFormat extends SequenceFileOutputFormat {
    public static SequenceFile.Reader[] getReaders(Configuration conf, Path dir)
	throws IOException {
	FileSystem fs = dir.getFileSystem(conf);
	FileOnlyPathFilter filter = new FileOnlyPathFilter(fs);
	Path[] names = FileUtil.stat2Paths(fs.listStatus(dir,filter));

	// sort names, so that hash partitioning works
	Arrays.sort(names);
    
	SequenceFile.Reader[] parts = new SequenceFile.Reader[names.length];
	for (int i = 0; i < names.length; i++) {
	    parts[i] = new SequenceFile.Reader(fs, names[i], conf);
	}
	return parts;
    }  
}