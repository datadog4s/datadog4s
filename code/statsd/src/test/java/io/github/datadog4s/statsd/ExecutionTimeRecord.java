package io.github.datadog4s.statsd;

import java.util.List;
import java.util.Objects;

public class ExecutionTimeRecord {
   private String aspect;
   private long executionTime;
   private double sampleRatio;
   private List<String> tags;

   public ExecutionTimeRecord(String aspect, long executionTime, double sampleRatio, List<String> tags) {
      this.aspect = aspect;
      this.executionTime = executionTime;
      this.sampleRatio = sampleRatio;
      this.tags = tags;
   }

   public long getExecutionTime() {
      return executionTime;
   }

   public double getSampleRatio() {
      return sampleRatio;
   }

   public List<String> getTags() {
      return tags;
   }

   public String getAspect() {
      return aspect;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ExecutionTimeRecord that = (ExecutionTimeRecord) o;
      return executionTime == that.executionTime &&
              Double.compare(that.sampleRatio, sampleRatio) == 0 &&
              Objects.equals(aspect, that.aspect) &&
              Objects.equals(tags, that.tags);
   }

   @Override
   public int hashCode() {
      return Objects.hash(aspect, executionTime, sampleRatio, tags);
   }
}
