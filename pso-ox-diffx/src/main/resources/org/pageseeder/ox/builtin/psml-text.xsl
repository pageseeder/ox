<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://pageseeder.com/pmsl"
  exclude-result-prefixes="#all">

  <xsl:output encoding="utf-8" method="text"/>


  <xsl:template match="block|blockxref|caption|cell|hcell|heading|item|list|nlist|para|preformat|table">
    <xsl:text>&#xa;</xsl:text>
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="br">
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="displaytitle"/>

</xsl:stylesheet>
