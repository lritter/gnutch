/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.streaming;

import java.io.*;
import java.net.URLDecoder;
import java.lang.RuntimeException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.SequenceFileInputFormat;

import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.util.StringUtils;

import org.apache.nutch.protocol.Content;

import org.json.JSONStringer;
import org.json.JSONException;

/** A generic Mapper bridge.
 *  It delegates operations to an external program via stdin and stdout.
 */
public class JSONPipeMapper extends PipeMapper<Text, Content, Text, Content> {

    public static class ContentInputFormat extends SequenceFileInputFormat<Text,Content> { }

    public void map(Text key, Content value, OutputCollector<Text, Content> output, Reporter reporter) throws IOException {
	// init
	if (outThread_ == null) {
	    startOutputThreads(output, reporter);
	}
	if (outerrThreadsThrowable != null) {
	    mapRedFinished();
	    throw new IOException ("MROutput/MRErrThread failed:"
				   + StringUtils.stringifyException(
								    outerrThreadsThrowable));
	}
	try {
	    // 1/4 Hadoop in
	    numRecRead_++;
	    maybeLogRecord();
	    if (debugFailDuring_ && numRecRead_ == 3) {
		throw new IOException("debugFailDuring_");
	    }

	    // 2/4 Hadoop to Tool
	    if (numExceptions_ == 0) {
		write(value.toJSONString());
		clientOut_.write('\n');
		clientOut_.flush();
	    }} catch (IOException io) {
	    numExceptions_++;
	    if (numExceptions_ > 1 || numRecWritten_ < minRecWrittenToEnableSkip_) {
		// terminate with failure
		String msg = logFailure(io);
		appendLogToJobLog("failure");
		mapRedFinished();
		throw new IOException(msg);
	    } else {
		// terminate with success:
		// swallow input records although the stream processor failed/closed
	    }
	}
    }
}