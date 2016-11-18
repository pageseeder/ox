<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  Default schema for checking a DOCX document post-simplification.
  
  Conventions:
   - the 'flag' attributes values indicate severity: 
      "debug", "info" (default for <sch:report>), "warn", "error" (default for <sch:report>), "fatal"
   -  '' (single quote) is used to emphasize the style, type of markup
   -  "" (double quote) is used to quote text to provide context

-->
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2" >

  <sch:title>Validation for importing DOCX</sch:title>

  <sch:ns prefix="w" uri="http://schemas.openxmlformats.org/wordprocessingml/2006/main"/>
  <sch:ns prefix="r" uri="http://schemas.openxmlformats.org/officeDocument/2006/relationships"/>
  <sch:ns prefix="ve" uri="http://schemas.openxmlformats.org/markup-compatibility/2006" />
  <sch:ns prefix="o" uri="urn:schemas-microsoft-com:office:office"/>
  <sch:ns prefix="r" uri="http://schemas.openxmlformats.org/officeDocument/2006/relationships"/> 
  <sch:ns prefix="m" uri="http://schemas.openxmlformats.org/officeDocument/2006/math" /> 
  <sch:ns prefix="v" uri="urn:schemas-microsoft-com:vml" />
  <sch:ns prefix="wp" uri="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" />
  <sch:ns prefix="w10" uri="urn:schemas-microsoft-com:office:word" />
  <sch:ns prefix="w" uri="http://schemas.openxmlformats.org/wordprocessingml/2006/main"/>
  <sch:ns prefix="wne" uri="http://schemas.microsoft.com/office/word/2006/wordml"/>

  <!-- ============================================================================================
       Rules to check that the data has been simplified as expected.
  -->
  <sch:pattern id="Simplification">

    <sch:title>DOCX Simplification</sch:title>

    <!-- Rule against the body as we don't want to report every instance -->
    <sch:rule context="w:body" >

        <sch:assert test="not(descendant::w:proofErr|descendant::w:noProof)" flag="error">Word document should not contain any proofing errors: <sch:value-of select="count(descendant::w:proofErr|descendant::w:noProof)"/> found</sch:assert>
        
        <sch:assert test="not(descendant::w:permEnd|descendant::w:permStart)" flag="error">Word document should not contain any permissions: <sch:value-of select="count(descendant::w:permEnd|descendant::w:permStart)"/> found</sch:assert>

    </sch:rule>

  </sch:pattern>


  <!-- ============================================================================================
       Rules that trap common markup issues which aren't generally supported
  -->
  <sch:pattern id="Aberrations">

    <sch:title>Aberrations</sch:title>

    <sch:rule context="w:tbl">
    
        <sch:assert test="not(descendant::w:tbl)" flag="fatal">A table inside a table is not supported</sch:assert>

        <sch:assert test="w:tr" flag="error">A table must contain at least one row: near text "<sch:value-of select="."/>"</sch:assert>

    </sch:rule>

    <sch:rule context="w:tr" >

      <sch:assert test="w:tc" flag="error">A row must contain at least one cell: near text "<sch:value-of select="."/>"</sch:assert>

    </sch:rule>

  </sch:pattern>


  <!-- ============================================================================================
       Useful reports
  -->
  <sch:pattern id="Info">
    
    <sch:rule context="w:body">
      
      <!-- Count the tables -->
      <sch:report test="descendant::w:tbl"><sch:value-of select="count(descendant::w:tbl)"/> tables found</sch:report>

      <!-- Count the paragraphs -->
      <sch:report test="descendant::w:p"><sch:value-of select="count(descendant::w:p)"/> paragraphs found</sch:report>

    </sch:rule>

  </sch:pattern>


  <!-- ============================================================================================
       All defined styles are here. 
   -->
  <sch:pattern id="Styles">

    <!-- 
          Character Formating
    -->
    <sch:rule context="w:p/w:rPr/w:b"><sch:assert test="true()" /></sch:rule>
    <sch:rule context="w:p/w:rPr/w:i"><sch:assert test="true()" /></sch:rule>
    <sch:rule context="w:p/w:rPr/w:u"><sch:assert test="true()" /></sch:rule>
    <sch:rule context="w:p/w:rPr/*">
      <sch:report test="true()" flag="warn">Formatting '<sch:name/>' will not be implemented</sch:report>
    </sch:rule>

    <!--
      Check paragraph styles 
    --> 
    <sch:rule context="w:pStyle">

      <!-- List supported paragraph styles -->
      <sch:let name="paragraph-styles" value="(
        'Pagetitle', 
        'Heading1',
        'Heading2',
        'Heading3',
        'Heading4',
        'Heading5',
        'Heading6',
        'Heading1numbered',
        'Heading2numbered', 
        'Heading3numbered', 
        'Heading4numbered',
        'Caption', 
        'ListParagraph', 
        'Normalcentre', 
        'Normalright', 
        'Small', 
        'Blockquote',
        'Primarybutton', 
        'Secondarybutton',
        'Indent1', 
        'Indent2', 
        'Indent3',
        'Tableheading', 
        'Tablecaption', 
        'Tableheadingcentre', 
        'Tableheadingright', 
        'Tablesummary',
        'Unformatted'
      )"/>

      <!-- Check that all styles in use are supported -->
      <sch:assert test="count(index-of($paragraph-styles, @w:val)) gt 0" >Unsupported style '<sch:value-of select="@w:val"/>' will be imported as paragraph: "<sch:value-of select="../../w:r/w:t"/>"</sch:assert>

      <!-- Warn the user when unformatted content is detected -->
      <sch:report test="@w:val='Unformatted'" flag="warn">Paragraph contains 'Unformatted' content: "<sch:value-of select="../../w:r/w:t"/>"</sch:report>

    </sch:rule>

  </sch:pattern>

</sch:schema>
