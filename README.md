## Web Service Documentation Generator ##

Automatically generate up-to-date documentation for your REST API.

#### Sample Input ####

    public class SnowReportController {
        
        @RequestMapping(value = "/snow-report/{mountainId}", method = GET)
        public SnowReport getReport(@PathVariable("mountainId") String mId) {
            ...
        }

        public interface SnowReport {
            public String getMountainName();
            public double getTemperatureInFahrenheit();
            public double getStormSnowfall();
            public double getOvernightSnowfall();
            public SkyReport getConditions();
        }

        public enum SkyReport {
            CLEAR, OVERCAST, SNOWING
        }
    }

#### Running wsdoc ####

TBD

#### Limitations ####

* wsdoc is currently limited to REST endpoints identified via the 
  Spring 3 Web Services annotations (@RequestMapping and whatnot).

* Only a subset of the Spring Web Services annotations are supported.
  This isn't by design or due to fundamental limitations; we've just
  only built support for the parts that we use.

#### License ####

wsdoc is licensed under the Apache Software License v2. The text of
the license is available here: http://www.apache.org/licenses/LICENSE-2.0.html
