[![Maven Central](https://img.shields.io/maven-central/v/org.pageseeder.ox/pso-ox-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.pageseeder.ox%22%20AND%20a:%22pso-ox-core%22)

# ox
An XML processing pipeline doing that hard work


## Model Example

```xml
<?xml version="1.0" encoding="utf-8"?>
<pipelines>
  <pipeline id="xml-validation" name="Validate XML" accepts="application/xml" default="true">

    <!-- 
      The pipeline can have the fields that need to be displayed in the front the end. 
      All option can be found here https://ps.pageseeder.com/ps/ui/g/dev-ox/d/207649.html?publicationid=oxv3
    -->
    <fields>

      <!-- If it is necessary to have a hidden input field -->
      <field
          type="hidden"
          id="config"
          name="config"
          value="custom.properties"
      />
      <!-- Text input field -->
      <field
          type="text"
          id="page-offset"
          name="page-offset"
          label="Offset"
          value="0"
          hint="The pages will increase with a value defined."
      />

      <!-- Switch input field [false or true]  -->
      <field
          type="switch"
          id="bookmarks"
          name="bookmarks"
          label="Bookmarks"
          active="false"
          hint="If active the system will try to create the pages according the bookmarks (SLOWER)."
      />


      <!-- Skip Steps -->
      <field
          type="string"
          label="Skip Steps"
          class="body-1"
      />
      <field
          type="skip"
          id="skip-first-two-steps"
          label="Skipping first two steps"
          values="step-id-1,step-id-2"
      />
      <field
          type="skip"
          id="s3-upload"
          label="S3 Upload"
          values="prepare-s3-upload,upload-pdfs-to-s3"
      />
    </fields>
    <step id="schematron-validation-asynchronous" async="true" name="XML Validation Asynchronous" class="org.pageseeder.ox.schematron.step.SchematronValidation">
      <parameter name="schema" value="schema.sch"/>
      <!-- Glob pattern for input -->
      <parameter name="input" value="*.xml"/>
      <parameter name="output" value="first-validation.xml"/>
    </step>
    <step id="schematron-validation-synchronous" name="XML Validation synchronous" class="org.pageseeder.ox.schematron.step.SchematronValidation">
      <parameter name="schema" value="schema.sch"/>
      <parameter name="input" value="first-validation.xml"/>
      <parameter name="output" value="second-validation.xml"/>
      <parameter name="dynamic-parameter-example" 
                 value="optional text before {extra-text-parameter-name=optional default value} optional text after"/>
    </step>    
    <!-- Comma separated list example for input parameter -->
    <step id="Zip" name="Zipping." class="org.pageseeder.ox.step.Compression">
      <parameter name="input" value="first-validation.xml,second-validation.xml" /> 
      <parameter name="output" value="validation.zip"/>
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
    <!-- 
      Following is the standard way to return the parameters/properties/infos.
      These information are optional, but if they are presented, then the front end will show them.       
    -->       
    
    <!-- This is one way to show the steps parameters and/or package data parameters (they are just examples) -->
    <parameters>
      <parameter name="output" value="converted"/>
      <parameter name="page-template" value="resources/custom-template-page.psml"/>
      <parameter name="config" value="resources/custom.properties"/>
      <parameter name="input" value="/1716-2012.pdf"/>
    </parameters>
    
    <!-- If the step uses any properties file which are used to process the file (they are just examples) -->
    <properties>
      <property name="bookmarks" value="false"/>
      <property name="bookmarks.guess.end.page" value="false"/>     
      <property name="image.creation" value="true"/>
      <property name="pages" value="1-9"/>
    </properties>
    
    <!-- In case the step wants to return an extra information. This is how it can be done -->
    <infos name="This PDF Metadata">
      <!-- 
        The headers will be used as table headers.
        The text is what will be displayed in the front end.
        The value will be use to match the information with the attribute name in the element info. 
        Only the attributes name from info that matches the value attribute text that will be displayed.
      -->
      <headers>        
        <!--
        The default text when the value is name and value are Name and Value.
        -->
        <header text="Page"        value="name"/>
        <header text="Description" value="value"/>
       <!--<header text="Type"     value="type"/>-->
        <header text="Source"      value="source"/>
      </headers>
      <!-- 
        Element info needs at least three mandatory attributes:
        - name
        - value
        - type: has three possible values string, list and map. This will indicate how the attribute value should be
        displayed in the front end. The default option is string.  
        
        It is possible to have extra attributes and if they are specified in the header element, they will also be 
        displayed. As example we add the source attribute which contains information about how it has been retrieved.        
      -->
      <info name="author" value="Standards Word" type="string" source="file metadata"/>
      <info name="bookmarks" value="true" type="string" source="Application checks if the file has outlines."/>
      <!-- info type list. The value has semicolon (';') separated list-->
      <info name="bookmarks_missing_destination" value="1.1 DEFINITIONS;1.1.1 Sub title" type="list" source="Application has an logic to find this information."/>
      <info name="bookmarks_valid" value="false" type="string" source="Application has a logic to find this information."/>
      <info name="creation_date" value="" type="string" source="file metadata"/>
      <info name="creator" value="" type="string" source="file metadata"/>
      <!-- 
      Info type map. The value contains a map, each item of this map is separated by semicolon (';'), the key and the value is separated by two points (':') and value also can contains a comma separated list.
      The label 'I' is in the page 1, 2 and 3.
      The label 'II' is in the page 4, 5 and 6.
      -->
      <info name="page_labels_repeated_map" value="I:1,2,3;II:4,5,6" type="map" source="Application has a logic to find this information."/>
    </infos>
  </result>
</step>
```
 
## Some examples of Glob Pattern (Input Parameter)

### By extension

__`*.java`__ - Matches all files with java extension into base directory. 

__`**.java`__ - Matches all files with java extension into base directory and sub directories.
 
__`**/*.java`__ - Matches all files with java extension into sub directories.
 

### By multiple extension
 
__`*.{html,java}`__ - Matches all files that has extension as html or java into base directory. if you want the  sub directories.

 
### By folder

__`*folder1/**`__ - Matches all files into sub directory folder1

 
### By single character
 
__`?.java`__ - Matches all files that has any single character as name and extension as java into base directory.
__`[abc].java`__ - Matches all files that has a or b or c as name and extension as java into base directory.
__`[!a].java`__ -  Matches all files that has any single character different of 'a' as name and extension as java into base directory.


## Dynamic Parameters

The request and step parameters can have its value connected to other parameter. 

Example:

There is a parameter called "ps-group-name" with value "test".
And there is another "input"="/{ps-group-name}/documents". 
The final value for "input" will be "/test/documents".

It allows a cycle reference limited to 2 levels
"text-1"="{text-2}" (first reference)
"text-2"="{text-3}" (second reference)
"text-3"="value-3"

The result for text-1 is value-3

This logic is in StepUtils class.

**Note: ** Each step needs to be updated to use this logic.
Steps that are using it
- Transformation

## Clean UP Files

There is a class called CleanUpManager which is responsible to clean the files generated by this application. However it needs to be started and stopped by calling its methods start and stop. 

Also the caller can add files to be ignored (addFileToIgnore) and remove them (removeFileToIgnore). If file is folder and it has subfiles, the subfiles will be deleted as well, except if they are added to be ignored.

if your application is using berlioz, then the web-fragment in ox-berlioz will trigger this class automatically.

```xml
  <servlet>
    <servlet-name>Initialiser</servlet-name>
    <servlet-class>org.pageseeder.berlioz.servlet.InitServlet</servlet-class>
    <init-param>
      <param-name>lifecycle-listener</param-name>
      <param-value>org.pageseeder.ox.berlioz.OXLifecycle</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
  </servlet>   
``` 