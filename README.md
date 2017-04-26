[ ![Download](https://api.bintray.com/packages/pageseeder/maven/ox/images/download.svg) ](https://bintray.com/pageseeder/maven/ox/_latestVersion)

# ox
An XML processing pipeline doing that hard work


## Model Example

```xml
<?xml version="1.0" encoding="utf-8"?>
<pipelines>
  <pipeline id="xml-validation" name="Validate XML" accepts="application/xml" default="true">
    <step id="schematron-validation-asynchronous" async="true" name="XML Validation Asynchronous" class="org.pageseeder.ox.schematron.step.SchematronValidation">
      <parameter name="schema" value="schema.sch"/>
    </step>
    <step id="schematron-validation-synchronous" name="XML Validation synchronous" class="org.pageseeder.ox.schematron.step.SchematronValidation">
      <parameter name="schema" value="schema.sch"/>
    </step>
  </pipeline>
  
  <pipeline id="xml-validation2" name="Validate XML(Synchronous)" accepts="application/xml" default="true">
    <step id="schematron-validation-asynchronous" async="false" name="XML Validation Synchronous" class="org.pageseeder.ox.schematron.step.SchematronValidation">
      <parameter name="schema" value="schema.sch"/>
    </step>
  </pipeline>
</pipelines>
``` 

## Step Response Example
```xml
<?xml version="1.0" encoding="utf-8"?>
<step id="conversion-psml" model="pdf" name="Converting" async="true" step="com.pageseeder.ox.pdf.step.PDFToPSML" next-id="zip-converted">
  <parameter name="output" value="converted"/>
  <parameter name="page-template" value="resources/custom-template-page.psml"/>
  <parameter name="config" value="resources/custom.properties"/>
  <result name="PDF Conversion" id="Process-ID" model="pdf" status="ok" time="14596" downloadable="false" path="converted" input="/pdf-file.pdf">
    <!-- It is optional in the step result -->
    <setup>
    
      <!-- Custom parameters defined in the model.xml for this step (they are just examples)-->
      <parameters>
        <parameter name="output" value="converted"/>
        <parameter name="page-template" value="resources/custom-template-page.psml"/>
        <parameter name="config" value="resources/custom.properties"/>
        <parameter name="input" value="/1716-2012.pdf"/>
      </parameters>
      
      <!-- Some properties values that were used to process the file (they are just examples) -->
      <properties>
        <property name="bookmarks" value="false"/>
        <property name="bookmarks.guess.end.page" value="false"/>     
        <property name="image.creation" value="true"/>
        <property name="pages" value="1-9"/>
      </properties>
    </setup>
    <!-- It is optional in the step result and is mainly used to return information of the file -->
    <infos name="PDF Test">
      <info name="author" value="Standards Word" type="string"/>
      <info name="bookmarks" value="true" type="string"/>
      <!-- info type list. The value has semicolon (';') separated list-->
      <info name="bookmarks_missing_destination" value="1.1 DEFINITIONS;1.1.1 Sub title" type="list"/>
      <info name="bookmarks_valid" value="false" type="string"/>
      <info name="creation_date" value="" type="string"/>
      <info name="creator" value="" type="string"/>
      <!-- 
      Info type map. The value contains a map, each item of this map is separated by semicolon (';'), the key and the value is separated by two points (':') and value also can contains a comma separated list.
      The label 'I' is in the page 1, 2 and 3.
      The label 'II' is in the page 4, 5 and 6.
      -->
      <info name="page_labels_repeated_map" value="I:1,2,3;II:4,5,6" type="map"/>
    </infos>
  </result>
</step>
``` 
