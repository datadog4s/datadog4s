package io.github.datadog4s.statsd;

import java.util.List;
import java.util.Objects;

public class HistogramRecord {
   private String aspect;
   private long value;
   private double sampleRatio;
   private List<String> tags;

   public HistogramRecord(String aspect, long value, double sampleRatio, List<String> tags) {
      this.aspect = aspect;
      this.value = value;
      this.sampleRatio = sampleRatio;
      this.tags = tags;
   }

   public long value() {
      return value;
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
      HistogramRecord that = (HistogramRecord) o;
      return value == that.value &&
              Double.compare(that.sampleRatio, sampleRatio) == 0 &&
              Objects.equals(aspect, that.aspect) &&
              Objects.equals(tags, that.tags);
   }

   @Override
   public int hashCode() {
      return Objects.hash(aspect, value, sampleRatio, tags);
   }
}
