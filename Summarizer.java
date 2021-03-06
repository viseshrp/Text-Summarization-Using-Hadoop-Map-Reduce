package com.cloud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

/*Created by Viseshprasad Rajendraprasad
vrajend1@uncc.edu
*/

public class Summarizer extends Configured implements Tool {

	private static final Logger LOG = Logger.getLogger(Summarizer.class);

	public static void main(String[] args) throws Exception {
		// Chain the term frequency and the TFIDF jobs using Toolrunner
		//int res_termfrequency = ToolRunner.run(new TermFrequency(), args);
		int res_summarizer = ToolRunner.run(new Summarizer(), args);
		System.exit(res_summarizer);
	}

	public int run(String[] args) throws Exception {
		Job job = Job.getInstance(getConf(), " summarizer ");

		Configuration configuration = job.getConfiguration(); // create a
																// configuration
																// reference
        String termList="";

		try{
			FileSystem fs = FileSystem.get(configuration);
			Path path = new Path(args[0]);
			BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(path)));
            String term;
            term=br.readLine();
            while (term != null){
                    termList += term + ",";
                    term=br.readLine();
            }
    }catch(Exception e){
    	e.printStackTrace();
    }
		
		configuration.set("termList", termList); // use
																	// configuration
																	// object to
																	// pass file
																	// count

		job.setJarByClass(this.getClass());
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		FileInputFormat.addInputPath(job, new Path(args[1]));
		FileOutputFormat.setOutputPath(job, new Path(args[2]));

		// Explicitly set key and value types of map and reduce output
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		//job.setNumReduceTasks(1);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static class Map extends Mapper<LongWritable, Text, Text, Text> {

		public void map(LongWritable offset, Text lineText, Context context) throws IOException, InterruptedException {

			String termList = context.getConfiguration().get("termList");
			String[] termListArray = termList.split(",");
			
			for (String term: termListArray){
			if(lineText.toString().toLowerCase().contains(term.toLowerCase()))
			context.write(new Text(""), lineText);
			}
		}
	}

	public static class Reduce extends Reducer<Text, Text, Text, Text> {
		@Override
		public void reduce(Text word, Iterable<Text> iterable, Context context)
				throws IOException, InterruptedException {

			// Loop through postings list to accumulate and find the document
			// frequency
			for (Text text : iterable) {
				// write to output
				context.write(new Text(""), text);
			}
		}

		}
	}