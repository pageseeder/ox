<!--
  This schematron validates PSML file for portable level

  @author Philip Rutherford
  @version 5 August 2013
-->

<!-- Uncomment this if using stand alone (it is generated dynamically in PageSeeder). -->
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron">

  <sch:let name="max-xrefs" value="3000" />

  <sch:ns prefix="ps" uri="http://www.pageseeder.com/editing/2.0"/>

  <!--
    Set of rules applying to the entire document
  -->
  <sch:pattern>

    <!-- Document -->
    <sch:rule context="document">
      
      <sch:let name="fragment-ids" value="section/fragment/@id  | section/xref-fragment/@id |
                                          section/properties-fragment/@id | section/media-fragment/@id "/>
      <sch:let name="distinct-fragment-ids" value="distinct-values(for $i in $fragment-ids return lower-case($i))"/>

      <!--
        Portable format cannot contain fragments element
       -->
      <sch:assert test="not(./@level='portable') or not(./fragments)">
        Portable format cannot contain fragments element.
      </sch:assert>

      <!--
        Portable format must have at least one section
       -->
      <sch:assert test="not(./@level='portable') or ./section">
        Portable format must contain at least one section.
      </sch:assert>

      <!--
        Metadata format cannot contain section or toc elements
       -->
      <sch:assert test="not(./@level='metadata') or not(./section or ./toc)">
        Metadata format cannot contain section or toc elements.
      </sch:assert>

      <!-- Maximum number of xrefs -->
      <sch:assert test="count(.//xref | ..//blockxref) &lt;= $max-xrefs">
        For performance reasons, the number of cross-references in a single document is limited to <sch:value-of select="$max-xrefs" />,
        this document contains <sch:value-of select="count(.//xref | .//blockxref)" />.
      </sch:assert>

      <!-- Same fragment ID with different case not allowed -->
      <sch:assert test="count($fragment-ids) = count($distinct-fragment-ids)">
        Same fragment ID with different case not allowed.
      </sch:assert>

    </sch:rule>

    <!-- Document Info -->
    <sch:rule context="documentinfo">

      <!-- Validate title -->
      <sch:assert test="string-length(uri/@title) &lt; 251">
        The document title cannot be more than 250 characters.
      </sch:assert>

      <!-- Validate document id -->
      <sch:assert test="string-length(uri/@docid) &lt; 101">
        The docid cannot be more than 100 characters.
      </sch:assert>

    </sch:rule>
 
    <!-- Properties Fragment -->
    <sch:rule context="properties-fragment/property">

      <!--
        Single value
       -->
      <sch:assert test="not(@value and (value or xref)) and not(value and xref)">
        The property element <sch:value-of select="@name" /> can only have a value attribute or value elements or xref elements.
      </sch:assert>

    </sch:rule>


    <!-- Tables -->
    <sch:rule context="table">

      <!-- Nested tables are not allowed -->
      <sch:assert test="not(ancestor::table)">Nested tables are not allowed.</sch:assert>
      
      <!-- The <col> elements must match the number of columns in the table -->
      <sch:assert test="not(col) or count(col[not(@span)]) + sum(col/@span) = count(row[1]/*[not(@colspan)]) + sum(row[1]/*/@colspan)">
        The &lt;col&gt; elements must match the number of columns in the table.
      </sch:assert>

    </sch:rule>


    <!-- Headings -->
    <sch:rule context="heading">

      <!-- Headings in tables are not allowed -->
      <sch:assert test="not(ancestor::table)">Headings are not allowed inside tables.</sch:assert>

      <!-- Heading cannot not have both 'prefix' and numbered="true" attributes -->
      <sch:assert test="not(@prefix and @numbered='true')">Heading cannot have both 'prefix' and numbered="true" attributes.</sch:assert>

    </sch:rule>

    <!-- Images -->
    <sch:rule context="image">

      <!-- src cannot start with http:// or https:// -->
      <sch:assert test="not(starts-with(@src,'http://') or starts-with(@src,'https://'))">Image src cannot start with http:// or https://.</sch:assert>

      <!-- One of src, uriid or docid is required -->
      <sch:assert test="@uriid or @docid or @src">One of src, uriid or docid is required on image element.</sch:assert>

    </sch:rule>

    <!-- Paras -->
    <sch:rule context="para">

      <!-- Para cannot have both 'prefix' and numbered="true" attributes -->
      <sch:assert test="not(@prefix and @numbered='true')">Para cannot have both 'prefix' and numbered="true" attributes.</sch:assert>

    </sch:rule>

    <!-- XRefs -->
    <sch:rule context="xref|blockxref">

      <!-- XRefs must have a 'frag' attribute -->
      <sch:assert test="@frag">XRefs must have a 'frag' attribute (it can be lost when copy/pasting HTML instead of the source).</sch:assert>

    </sch:rule>
    
  </sch:pattern>

</sch:schema>
