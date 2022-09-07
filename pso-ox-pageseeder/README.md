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
