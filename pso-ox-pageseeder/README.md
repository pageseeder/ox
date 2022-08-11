[![Maven Central](https://img.shields.io/maven-central/v/org.pageseeder.ox/pso-ox-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.pageseeder.ox%22%20AND%20a:%22pso-ox-core%22)

# Classes



## BulkGroupPublish

It receives as input a xml and call the publishing script for each publish. 

At the end it writes a report with the result of each calling.

Another parameter it can receive is the interval that will be to define the interval to check the status.

XML Input Example: 

```xml
<?xml version="1.0" encoding="utf-8"?>
<publishes>
  <publish project="projectname"
           group="groupname"
           member=""
           target="ant-task-name"
           type="PROCESS"
           log-level="INFO"/>
  <publish project="anotherproject"
           group="groupname2"
           member=""
           target="ant-task-name2"
           type="PUBLISH"
           log-level="INFO"/>
  <publish project="projectname"
           group="groupname3"
           member=""
           target="ant-task-name"
           type="PROCESS"
           log-level="INFO"/>
</publishes>
