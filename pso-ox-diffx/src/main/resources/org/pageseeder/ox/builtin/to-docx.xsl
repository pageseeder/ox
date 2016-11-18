<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ve="http://schemas.openxmlformats.org/markup-compatibility/2006"
  xmlns:o="urn:schemas-microsoft-com:office:office"
  xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
  xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math"
  xmlns:v="urn:schemas-microsoft-com:vml"
  xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
  xmlns:w10="urn:schemas-microsoft-com:office:word"
  xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
  xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml"
  xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"
  xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
  xmlns:rs="http://schemas.openxmlformats.org/package/2006/relationships"
  xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
  xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/"
  xmlns:ps="http://www.pageseeder.com/editing/2.0" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#all">

<xsl:output encoding="utf-8" method="xml" indent="no"/>

<xsl:template match="html">
<w:document>
  <xsl:apply-templates select="body"/>
</w:document>
</xsl:template>

<xsl:template match="head"/>

<xsl:template match="body">
  <w:body>
    <xsl:apply-templates/>
  </w:body>
</xsl:template>

<xsl:template match="h1|h2|h3|h4|h5|p">
<w:p>
  <w:r>
    <w:t><xsl:value-of select="."/></w:t>
  </w:r>
</w:p>
</xsl:template>

</xsl:stylesheet>
