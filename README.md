[ ![Download](https://api.bintray.com/packages/pageseeder/maven/ox/images/download.svg) ](https://bintray.com/pageseeder/maven/ox/_latestVersion)

# ox
An XML processing pipeline doing that hard work


## Model Example

```xml
<?xml version="1.0" encoding="utf-8"?>
<pipelines>

  <pipeline id="docx-to-psml" name="DOCX to PSML" accepts="application/vnd.openxmlformats-officedocument.wordprocessingml.document" default="true">
    <step id="docx-to-psml" name="Import Word document (.docx) as PageSeeder XML (.psml)" class="org.pageseeder.docx.ox.step.DOCXToPSML">
      <parameter name="output" value="myoutput/generic.psml" />
      <parameter name="config" value="docx-to-psml/word-import-config.xml" />
    </step>
  </pipeline>

  <pipeline id="psml-to-docx" name="PSML to DOCX" accepts=".psml">
    <step id="psml-to-docx" name="Import PageSeeder XML (.psml) as Word document (.docx)" class="org.pageseeder.docx.ox.step.PSMLToDOCX">
      <parameter name="output" value="myoutput/generic.docx" />
      <parameter name="config" value="psml-to-docx/word-export-config.xml" />
      <parameter name="dotx" value="psml-to-docx/word-export-template.docx" />
    </step>
  </pipeline>
 
</pipelines>
``` 