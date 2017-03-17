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