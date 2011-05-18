## Web Service Documentation Generator ##

Automatically generate up-to-date documentation for your REST API

#### License ####

wsdoc is licensed under the Apache Software License v2. The text of
the license is available here: http://www.apache.org/licenses/LICENSE-2.0.html

#### Limitations ####

* wsdoc is currently limited to REST endpoints identified via the 
  Spring 3 Web Services annotations (@RequestMapping and whatnot).

* Only a subset of the Spring Web Services annotations are supported.
  This isn't by design or due to fundamental limitations; we've just
  only built support for the parts that we use.
