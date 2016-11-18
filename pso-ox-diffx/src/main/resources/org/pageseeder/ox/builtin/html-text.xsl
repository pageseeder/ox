<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#all">

<xsl:output encoding="utf-8" method="text"/>

<xsl:template match="head"/>

<xsl:template match="h1|h2|h3|h4|h5|h6|li|td|p|div">
<xsl:text>&#xa;</xsl:text>
<xsl:apply-templates />
</xsl:template>

<xsl:template match="br">
<xsl:text>&#xa;</xsl:text>
</xsl:template>

</xsl:stylesheet>
