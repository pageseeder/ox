<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
  exclude-result-prefixes="#all">

<xsl:output encoding="utf-8" method="text"/>

<xsl:template match="/">
  <xsl:apply-templates select="//w:p"/>
</xsl:template>

<xsl:template match="w:p">
  <xsl:text>&#xa;</xsl:text>
  <xsl:apply-templates select="descendant::w:t|descendant::w:br"/>
</xsl:template>

<xsl:template match="w:br">
  <xsl:text>&#xa;</xsl:text>
</xsl:template>

<xsl:template match="w:t">
  <xsl:value-of select="."/> 
</xsl:template>

</xsl:stylesheet>
