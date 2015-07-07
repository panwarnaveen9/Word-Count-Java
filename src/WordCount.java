import java.io.IOException;
import java.lang.InterruptedException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordCount {
    private final static IntWritable ONE = new IntWritable(1);
    private final static Pattern WORD = Pattern.compile("\\w+");

    public static class WordCountMapper 
            extends Mapper<LongWritable, Text, Text, IntWritable> {
        private final Text word = new Text();

        @Override
        public void map(LongWritable key, Text value, Context context) 
                throws IOException, InterruptedException {

            String valueString = value.toString();
            Matcher matcher = WORD.matcher(valueString);
            while (matcher.find()) {
                word.set(matcher.group().toLowerCase());
                context.write(word, ONE);
            }
        }
    }

    public static class WordCountReducer 
            extends Reducer<Text, IntWritable, Text, IntWritable> {
        private final IntWritable totalCount = new IntWritable();

        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context) 
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable value : values) {
                sum += value.get();
            }
            totalCount.set(sum);
            context.write(key, totalCount);
        }
    }

    public static void main(String[] args) 
            throws IOException, ClassNotFoundException, InterruptedException {

        if (args.length != 2) {
            System.err.println("Usage: MapRWordCount <input_path> <output_path>");
            System.exit(-1);
        }

        Job job = new Job();
        job.setJarByClass(WordCount.class);
        job.setJobName("MapReduce Word Count");

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setMapperClass(WordCountMapper.class);
        job.setCombinerClass(WordCountReducer.class);
        job.setReducerClass(WordCountReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}