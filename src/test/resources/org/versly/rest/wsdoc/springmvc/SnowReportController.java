/*
 * Copyright 2011 TaskDock, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.versly.rest.wsdoc.springmvc;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class SnowReportController {

    /**
     * Retrieves the current snow report for the specified mountain.
     */
    @RequestMapping(value = "/snow-report/{mountainId}", method = RequestMethod.GET)
    public SnowReport getReportForMountain(
            @PathVariable("mountainId") String mountainId) {
        return null;
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
