# Classes

## BulkAddURIMetadata

It receives as input a xml and call a service to change the metadatas for each uriid.

At the end it writes a report with the result of each calling.

Parameters:

- psconfig: The default value is the default configuration.
- input: if it is not informed it will use the uploaded file.
- output: it is required, but if it is not informed than create with a random name.
- interval: the default value is 100 milliseconds
- psgroup: it is required

XML Input Example:

```xml
<?xml version="1.0" encoding="utf-8"?>
<metadatas>
  <metadata>
    <uriid>123456</uriid>
    <draft>false</draft>
    <html>false</html>
    <last-modified>2023-01-01T01:01:01</last-modified>
    <note>Some texts</note>
    <note-labels>label01,label02</note-labels>
    <note-title>Note's title</note-title>
    <transclude>true</transclude>
    <properties>
      <property name="year" title="Year" type="string" value="2020" />
      <property name="year_month" title="Year" type="string" value="2020-01" />
      <property name="publish_date" title="Publish Date" type="date" value="2019-12-06" />
    </properties>
  </metadata>
</metadatas>
```

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
```