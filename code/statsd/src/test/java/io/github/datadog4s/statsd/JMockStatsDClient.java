package io.github.datadog4s.statsd;

import com.timgroup.statsd.Event;
import com.timgroup.statsd.ServiceCheck;
import com.timgroup.statsd.StatsDClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class JMockStatsDClient implements StatsDClient {
   private final AtomicReference<ArrayList<Object>> history;

   public JMockStatsDClient(AtomicReference<ArrayList<Object>> history) {
      this.history = history;
   }

   public AtomicReference<ArrayList<Object>> getHistory() {
      return history;
   }

   @Override
   public void stop() {
      
   }

   @Override
   public void close() {

   }

   @Override
   public void count(String aspect, long delta, String... tags) {

   }

   @Override
   public void count(String aspect, long delta, double sampleRate, String... tags) {

   }

   @Override
   public void count(String aspect, double delta, String... tags) {

   }

   @Override
   public void count(String aspect, double delta, double sampleRate, String... tags) {

   }

   @Override
   public void countWithTimestamp(String aspect, long value, long timestamp, String... tags) {
      
   }

   @Override
   public void countWithTimestamp(String aspect, double value, long timestamp, String... tags) {

   }

   @Override
   public void incrementCounter(String aspect, String... tags) {

   }

   @Override
   public void incrementCounter(String aspect, double sampleRate, String... tags) {

   }

   @Override
   public void increment(String aspect, String... tags) {

   }

   @Override
   public void increment(String aspect, double sampleRate, String... tags) {

   }

   @Override
   public void decrementCounter(String aspect, String... tags) {

   }

   @Override
   public void decrementCounter(String aspect, double sampleRate, String... tags) {

   }

   @Override
   public void decrement(String aspect, String... tags) {

   }

   @Override
   public void decrement(String aspect, double sampleRate, String... tags) {

   }

   @Override
   public void recordGaugeValue(String aspect, double value, String... tags) {

   }

   @Override
   public void recordGaugeValue(String aspect, double value, double sampleRate, String... tags) {

   }

   @Override
   public void recordGaugeValue(String aspect, long value, String... tags) {

   }

   @Override
   public void recordGaugeValue(String aspect, long value, double sampleRate, String... tags) {

   }

   @Override
   public void gauge(String aspect, double value, String... tags) {

   }

   @Override
   public void gauge(String aspect, double value, double sampleRate, String... tags) {

   }

   @Override
   public void gauge(String aspect, long value, String... tags) {

   }

   @Override
   public void gauge(String aspect, long value, double sampleRate, String... tags) {

   }

   @Override
   public void gaugeWithTimestamp(String aspect, double value, long timestamp, String... tags) {

   }

   @Override
   public void gaugeWithTimestamp(String aspect, long value, long timestamp, String... tags) {

   }

   @Override
   public void recordExecutionTime(String aspect, long timeInMs, String... tags) {

   }

   @Override
   public void recordExecutionTime(String aspect, long timeInMs, double sampleRate, String... tags) {
      history.updateAndGet(h -> {
         h.add(new ExecutionTimeRecord(aspect, timeInMs, sampleRate, Arrays.asList(tags)));
         return h;
      });
   }

   @Override
   public void time(String aspect, long value, String... tags) {

   }

   @Override
   public void time(String aspect, long value, double sampleRate, String... tags) {

   }

   @Override
   public void recordHistogramValue(String aspect, double value, String... tags) {

   }

   @Override
   public void recordHistogramValue(String aspect, double value, double sampleRate, String... tags) {

   }

   @Override
   public void recordHistogramValue(String aspect, long value, String... tags) {

   }

   @Override
   public void recordHistogramValue(String aspect, long value, double sampleRate, String... tags) {
      history.updateAndGet(h -> {
         h.add(new HistogramRecord(aspect, value, sampleRate, Arrays.asList(tags)));
         return h;
      });
   }

   @Override
   public void histogram(String aspect, double value, String... tags) {

   }

   @Override
   public void histogram(String aspect, double value, double sampleRate, String... tags) {

   }

   @Override
   public void histogram(String aspect, long value, String... tags) {

   }

   @Override
   public void histogram(String aspect, long value, double sampleRate, String... tags) {

   }

   @Override
   public void recordDistributionValue(String aspect, double value, String... tags) {

   }

   @Override
   public void recordDistributionValue(String aspect, double value, double sampleRate, String... tags) {

   }

   @Override
   public void recordDistributionValue(String aspect, long value, String... tags) {

   }

   @Override
   public void recordDistributionValue(String aspect, long value, double sampleRate, String... tags) {

   }

   @Override
   public void distribution(String aspect, double value, String... tags) {

   }

   @Override
   public void distribution(String aspect, double value, double sampleRate, String... tags) {

   }

   @Override
   public void distribution(String aspect, long value, String... tags) {

   }

   @Override
   public void distribution(String aspect, long value, double sampleRate, String... tags) {

   }

   @Override
   public void recordEvent(Event event, String... tags) {

   }

   @Override
   public void recordServiceCheckRun(ServiceCheck sc) {

   }

   @Override
   public void serviceCheck(ServiceCheck sc) {

   }

   @Override
   public void recordSetValue(String aspect, String value, String... tags) {

   }
}
