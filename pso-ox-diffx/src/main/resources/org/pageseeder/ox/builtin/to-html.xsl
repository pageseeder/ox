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
   exclude-result-prefixes="#all">

<xsl:output encoding="ascii" method="html" indent="yes" />

<xsl:template match="/">
  <!-- Only process body -->
  <xsl:apply-templates select="descendant::w:body"/>
</xsl:template>

<!-- ========================================================================================== -->
<!-- PARAGRAPHS                                                                                 -->
<!-- ========================================================================================== -->

<!--
  Default rule for paragraphs
 -->
<xsl:template match="w:p">
  <xsl:if test="w:r[w:br/@w:type='page']">
      <atopagebreak/>
  </xsl:if>
  <xsl:if test="w:r or w:hlink">
    <xsl:choose>
      <xsl:when test="w:pPr/w:pStyle/@w:val='Pagetitle'" >
        <h1><xsl:apply-templates /></h1>
      </xsl:when>
      <xsl:when test="w:pPr/w:pStyle/@w:val='Heading1'" >
        <h2><xsl:apply-templates /></h2>
      </xsl:when>
      <xsl:when test="w:pPr/w:pStyle/@w:val='Heading2'" >
        <h3><xsl:apply-templates /></h3>
      </xsl:when>
      <xsl:when test="w:pPr/w:pStyle/@w:val='Heading3'" >
        <h4><xsl:apply-templates /></h4>
      </xsl:when>
      <xsl:when test="w:pPr/w:pStyle/@w:val='Heading4'" >
        <h5><xsl:apply-templates /></h5>
      </xsl:when>
      <xsl:when test="w:pPr/w:pStyle/@w:val='Blockquote'" >
        <blockquote><xsl:apply-templates /></blockquote>
      </xsl:when>
      <xsl:when test="w:pPr/w:pStyle/@w:val='Small'" >
        <small><xsl:apply-templates /></small>
      </xsl:when>
      <xsl:when test="w:pPr/w:pStyle/@w:val='Indent1'" >
        <p class="indent1"><xsl:apply-templates /></p>
      </xsl:when>
      <xsl:when test="w:pPr/w:pStyle/@w:val='Indent2'" >
        <p class="indent2"><xsl:apply-templates /></p>
      </xsl:when>
      <xsl:when test="w:pPr/w:pStyle/@w:val='Indent3'" >
        <p class="indent3"><xsl:apply-templates /></p>
      </xsl:when>
      <xsl:when test="w:pPr/w:pStyle/@w:val='Primarybutton'" >
        <a class="button primary" href="w:hlink/@w:dest">
          <!-- XXX? -->
          <xsl:call-template name="concatSameStyleRun">
            <xsl:with-param name="currentNode" select ="w:hlink/child::*" />
          </xsl:call-template>
        </a>
      </xsl:when>
      <xsl:when test="w:pPr/w:pStyle/@w:val='Secondarybutton'" >
        <a class="button secondary" href="{w:hlink/@w:dest}">
          <!-- XXX? -->
          <xsl:call-template name="concatSameStyleRun">
            <xsl:with-param name="currentNode" select ="w:hlink/child::*" />
          </xsl:call-template>
        </a>
      </xsl:when>
      <xsl:when test="w:pPr/w:pStyle/@w:val='Tableheading'" >
        <xsl:apply-templates />
      </xsl:when>
      <xsl:when test="w:pPr/w:pStyle/@w:val='Normalright'" >
        <p class="alignRight"><xsl:apply-templates /></p>
      </xsl:when>
      <xsl:when test="w:pPr/w:pStyle/@w:val='Normalcentre'" >
        <p class="alignCentre"><xsl:apply-templates /></p>
      </xsl:when>
      <xsl:when test="w:pPr/w:pStyle/@w:val='Tableheadingright'" >
        <xsl:apply-templates />
      </xsl:when>
      <xsl:when test="w:pPr/w:pStyle/@w:val='Tableheadingcentre'" >
        <xsl:apply-templates />
      </xsl:when>
      <!--Make all paragraphs which has a style that are unhandled as unstyled-->
      <xsl:when test="w:pPr/w:pStyle/@w:val" >
        <span class="unstyled"><xsl:apply-templates /></span>
      </xsl:when>
      <!--All paragraphs without a style is handle as the Normal style -->
      <xsl:otherwise>
        <xsl:if test="count(descendant::w:r/w:t) > 0">
          <!-- XXX? -->
          <p><xsl:apply-templates /></p>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:if>
</xsl:template>

<!--
  Images
-->
<xsl:template match="w:p[w:r/w:pict]" priority="1">
  <xsl:param name="path" select="./w:r/w:pict/v:shape" />
  <p><img src="{$path/v:imagedata/@o:href}" alt="{$path/@alt}"/></p>
</xsl:template>

<!-- ========================================================================================== -->
<!-- LISTS                                                                                      -->
<!-- ========================================================================================== -->

<!--
   List Items
   Handles the first List Items and creates the lists
-->
<xsl:template match="w:p[w:pPr/w:listPr]" priority="3">
  <xsl:variable name="listID"  select="./w:pPr/w:listPr/w:ilfo/@w:val"/>
  <xsl:variable name="listLvl" select="./w:pPr/w:listPr/w:ilvl/@w:val"/>
  <xsl:choose>
    <xsl:when test="w:pPr/w:pStyle/@w:val='Heading1numbered'" >
      <h2 class="numbered">
        <xsl:value-of select="w:pPr/w:listPr/w:t/@w:val" />
        <xsl:text> </xsl:text>
        <xsl:apply-templates />
      </h2>
    </xsl:when>
    <xsl:when test="w:pPr/w:pStyle/@w:val='Heading2numbered'" >
      <h3 class="numbered">
        <xsl:value-of select="w:pPr/w:listPr/w:t/@w:val" />
        <xsl:text> </xsl:text>
        <xsl:apply-templates />
      </h3>
    </xsl:when>
    <xsl:when test="w:pPr/w:pStyle/@w:val='Heading3numbered'" >
      <h4 class="numbered">
        <xsl:value-of select="w:pPr/w:listPr/w:t/@w:val" />
        <xsl:text> </xsl:text>
        <xsl:apply-templates />
      </h4>
    </xsl:when>
    <xsl:when test="w:pPr/w:pStyle/@w:val='Heading4numbered'" >
      <h5 class="numbered">
        <xsl:value-of select="w:pPr/w:listPr/w:t/@w:val" />
        <xsl:text> </xsl:text>
        <xsl:apply-templates />
      </h5>
    </xsl:when>
    <!-- All list items thats already processed in the first list item section are ignored -->
    <xsl:when test=" preceding-sibling::w:p[1][./w:pPr/w:listPr] and (
              preceding-sibling::w:p[1][./w:pPr/w:listPr/w:ilfo/@w:val = $listID] or
              preceding-sibling::w:p[1][./w:pPr/w:listPr/w:ilvl/@w:val &lt; $listLvl] or
              preceding-sibling::w:p[1][./w:pPr/w:listPr/w:ilfo/@w:val = $listID][w:pPr/w:listPr/w:ilvl/@w:val = $listLvl] or
              preceding-sibling::w:p[1][./w:pPr/w:listPr][./w:pPr/w:listPr/w:ilfo/@w:val = $listID])"/>
    <xsl:otherwise>
      <xsl:call-template name="createListTags">
        <xsl:with-param name="currentListItem" select ="." />
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!--Handles/Creates Opening and closing UL/OL Tags-->
<xsl:template name="createListTags">
  <xsl:param name="currentListItem"></xsl:param>
  <xsl:variable name="listID" select="$currentListItem/w:pPr/w:listPr/w:ilfo/@w:val"/>
  <xsl:variable name="listDefId" select="ancestor::w:wordDocument/w:lists/w:list[@w:ilfo=$listID]/w:ilst/@w:val"/>
  <xsl:variable name="listStyleLink" select="ancestor::w:wordDocument/w:lists/w:listDef[@w:listDefId=$listDefId]/w:listStyleLink/@w:val"/>
  <xsl:variable name="styleLink" select="ancestor::w:wordDocument/w:lists/w:listDef[@w:listDefId=$listDefId]/w:styleLink/@w:val"/>
  <xsl:variable name="previousListID" select="$currentListItem/preceding-sibling::w:p[1]/w:pPr/w:listPr/w:ilfo/@w:val"/>
  <xsl:variable name="previousListDefId" select="ancestor::w:wordDocument/w:lists/w:list[@w:ilfo=$previousListID]/w:ilst/@w:val"/>
  <xsl:variable name="previousListStyleLink" select="ancestor::w:wordDocument/w:lists/w:listDef[@w:listDefId=$previousListDefId]/w:listStyleLink/@w:val"/>
  <xsl:variable name="previousStyleLink" select="ancestor::w:wordDocument/w:lists/w:listDef[@w:listDefId=$previousListDefId]/w:styleLink/@w:val"/>

  <xsl:choose>
     <xsl:when test="$listStyleLink = 'Numberedlistnumericstart' or
                     $styleLink = 'Numberedlistnumericstart' ">
         <ol>
             <xsl:call-template name="createListItem">
                 <xsl:with-param name="currentListItem" select ="$currentListItem" />
             </xsl:call-template>
         </ol>
     </xsl:when>
     <xsl:when test="$listStyleLink = 'Numberedlistalphastart' or
                     $styleLink = 'Numberedlistalphastart' ">
         <!--Do not apply redundant lowerAlpha class on nested lists-->
         <xsl:choose>
             <xsl:when test="$previousListStyleLink = 'Numberedlistalphastart' or
                             $previousStyleLink = 'Numberedlistalphastart' ">
                 <ol>
                     <xsl:call-template name="createListItem">
                         <xsl:with-param name="currentListItem" select ="$currentListItem" />
                     </xsl:call-template>
                 </ol>
             </xsl:when>
             <xsl:otherwise>
                 <ol class="lowerAlpha">
                     <xsl:call-template name="createListItem">
                         <xsl:with-param name="currentListItem" select ="$currentListItem" />
                     </xsl:call-template>
                 </ol>
             </xsl:otherwise>
         </xsl:choose>
     </xsl:when>
     <xsl:when test="$listStyleLink = 'Bulletedlist' or
                     $styleLink = 'Bulletedlist' ">
         <ul>
             <xsl:call-template name="createListItem">
                 <xsl:with-param name="currentListItem" select ="$currentListItem" />
             </xsl:call-template>
         </ul>
     </xsl:when>
     <xsl:when test="$listStyleLink = 'Blockquotelist' or
                     $styleLink = 'Blockquotelist' ">
         <xsl:choose>
             <xsl:when test="$previousListStyleLink = 'Blockquotelist' or
                             $previousStyleLink = 'Blockquotelist' ">
                 <ul>
                     <xsl:call-template name="createListItem">
                         <xsl:with-param name="currentListItem" select ="$currentListItem" />
                     </xsl:call-template>
                 </ul>
             </xsl:when>
             <xsl:otherwise>
                 <ul class="indent1">
                     <xsl:call-template name="createListItem">
                         <xsl:with-param name="currentListItem" select ="$currentListItem" />
                     </xsl:call-template>
                 </ul>
             </xsl:otherwise>
         </xsl:choose>
     </xsl:when>
     <xsl:when test="$listStyleLink = 'Redundant' or $styleLink = 'Redundant' ">
       <xsl:choose>
           <xsl:when test="$previousListStyleLink = 'Redundant' or
                           $previousStyleLink = 'Redundant' ">
               <ul>
                   <xsl:call-template name="createListItem">
                       <xsl:with-param name="currentListItem" select ="$currentListItem" />
                   </xsl:call-template>
               </ul>
           </xsl:when>
           <xsl:otherwise>
               <ul class="indent2">
                   <xsl:call-template name="createListItem">
                       <xsl:with-param name="currentListItem" select ="$currentListItem" />
                   </xsl:call-template>
               </ul>
           </xsl:otherwise>
       </xsl:choose>
     </xsl:when>
     <xsl:otherwise>
       <span class="undefined undefined-list">
         <xsl:call-template name="createListItem">
           <xsl:with-param name="currentListItem" select ="$currentListItem" />
         </xsl:call-template>
       </span>
     </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!--Handles List Item-->
<xsl:template name="createListItem">
    <xsl:param name="currentListItem"></xsl:param>
    <xsl:variable name="listID" select="$currentListItem/w:pPr/w:listPr/w:ilfo/@w:val"/>
    <xsl:variable name="listLvl" select="$currentListItem/w:pPr/w:listPr/w:ilvl/@w:val"/>
    <xsl:variable name="nestedListItem" select ="$currentListItem/following-sibling::w:p[1][./w:pPr/w:listPr/w:ilvl/@w:val]/w:pPr/w:listPr/w:ilvl/@w:val > $listLvl"/>

    <li>
        <xsl:apply-templates select ="$currentListItem/child::*" />
        <xsl:choose>
            <xsl:when test="$currentListItem/following-sibling::w:p[1][./w:pPr/w:listPr/w:ilvl/@w:val]/w:pPr/w:listPr/w:ilvl/@w:val > $listLvl ">
                <xsl:call-template name="createListTags">
                    <xsl:with-param name="currentListItem" select ="$currentListItem/following-sibling::w:p[1][./w:pPr/w:listPr/w:ilfo/@w:val!='']" />
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </li>

    <xsl:choose>
        <!-- Handles the next/following list item-->
        <xsl:when test="$currentListItem/following-sibling::w:p[1][./w:pPr/w:listPr/w:ilfo/@w:val]/w:pPr/w:listPr/w:ilfo/@w:val=$listID and
                          $currentListItem/following-sibling::w:p[1][./w:pPr/w:listPr/w:ilvl/@w:val]/w:pPr/w:listPr/w:ilvl/@w:val=$listLvl">
            <xsl:call-template name="createListItem">
                <xsl:with-param name="currentListItem" select ="$currentListItem/following-sibling::w:p[1][./w:pPr/w:listPr/w:ilfo/@w:val!='']" />
            </xsl:call-template>
        </xsl:when>
        <!-- Handles the next list item of the same level after a nested list -->
        <xsl:when test="$nestedListItem">
            <xsl:variable name="nextListNode" select="$currentListItem/following-sibling::w:p[w:pPr/w:listPr/w:ilfo/@w:val=$listID][w:pPr/w:listPr/w:ilvl/@w:val = $listLvl][1]"/>
            <!--Check that the next list item is not just a cont. of a list further down the document-->
            <xsl:choose>
                <xsl:when test="$nextListNode and $nextListNode/preceding-sibling::w:p[1]/w:pPr/w:listPr/w:ilvl/@w:val > $listLvl">
                    <xsl:call-template name="createListItem">
                        <xsl:with-param name="currentListItem" select ="$nextListNode" />
                    </xsl:call-template>
                </xsl:when>
            </xsl:choose>
        </xsl:when>
    </xsl:choose>
</xsl:template>
<!--##End List Items##-->

<!-- ========================================================================================== -->
<!-- CALL OUTS                                                                                  -->
<!-- ========================================================================================== -->

<!--Call outs-->
<xsl:template match="w:tbl[w:tblPr/w:tblStyle/@w:val='Callout-Attention']">
  <div class='attention'>
    <img class='icon' alt='Attention' src='/uploadedImages/Content/Images/attention.png'/>
    <xsl:apply-templates />
    <span class='visuallyHidden'>End of attention</span>
  </div>
</xsl:template>

<xsl:template match="w:tbl[w:tblPr/w:tblStyle/@w:val='Callout-Complete']">
  <div class='callout complete'>
    <img class='icon' alt='complete' src='/uploadedImages/Content/Images/complete.png'/>
    <xsl:apply-templates />
    <span class='visuallyHidden'>End of complete</span>
  </div>
</xsl:template>

<xsl:template match="w:tbl[w:tblPr/w:tblStyle/@w:val='Callout-Danger']">
  <div class='danger'>
    <img class='icon' alt='Danger' src='/uploadedImages/Content/Images/danger.png'/>
    <xsl:apply-templates />
    <span class='visuallyHidden'>End of danger</span>
  </div>
</xsl:template>

<xsl:template match="w:tbl[w:tblPr/w:tblStyle/@w:val='Callout-Direction']">
  <div class='direction'>
    <img class='icon' alt='Further information' src='/uploadedImages/Content/Images/direction.png'/>
    <xsl:apply-templates />
    <span class='visuallyHidden'>End of further information</span>
  </div>
</xsl:template>

<xsl:template match="w:tbl[w:tblPr/w:tblStyle/@w:val='Callout-Direction-findoutmore']">
  <div class='callout direction findOutMore'>
    <img class='icon' alt='' src='/uploadedImages/Content/Images/direction-find-out-more.png'/>
    <xsl:apply-templates />
    <span class='visuallyHidden'>End of find out more</span>
  </div>
</xsl:template>

<xsl:template match="w:tbl[w:tblPr/w:tblStyle/@w:val='Callout-Direction-getitdone']">
  <div class='callout direction getItDone'>
    <img class='icon' alt='' src='/uploadedImages/Content/Images/direction-get-it-done.png'/>
    <xsl:apply-templates />
    <span class='visuallyHidden'>End of get it done</span>
  </div>
</xsl:template>

<xsl:template match="w:tbl[w:tblPr/w:tblStyle/@w:val='Callout-Direction-help']">
  <div class='callout direction help'>
    <img class='icon' alt='' src='/uploadedImages/Content/Images/direction-help.png'/>
    <xsl:apply-templates />
    <span class='visuallyHidden'>End of help</span>
  </div>
</xsl:template>

<xsl:template match="w:tbl[w:tblPr/w:tblStyle/@w:val='Callout-Direction-listen']">
  <div class='callout direction listen'>
    <img class='icon' alt='' src='/uploadedImages/Content/Images/direction-listen.png'/>
    <xsl:apply-templates />
    <span class='visuallyHidden'>End of listen</span>
  </div>
</xsl:template>

<xsl:template match="w:tbl[w:tblPr/w:tblStyle/@w:val='Callout-Direction-watch']">
  <div class='callout direction watch'>
    <img class='icon' alt='' src='/uploadedImages/Content/Images/direction-watch.png'/>
    <xsl:apply-templates />
    <span class='visuallyHidden'>End of watch</span>
  </div>
</xsl:template>

<xsl:template match="w:tbl[w:tblPr/w:tblStyle/@w:val='Callout-Direction-workitout']">
  <div class='callout direction workItOut'>
    <img class='icon' alt='' src='/uploadedImages/Content/Images/direction-work-it-out.png'/>
    <xsl:apply-templates />
    <span class='visuallyHidden'>End of work it out</span>
  </div>
</xsl:template>

<xsl:template match="w:tbl[w:tblPr/w:tblStyle/@w:val='Callout-Error']">
  <div class='callout error'>
    <img class='icon' alt='error' src='/uploadedImages/Content/Images/error.png'/>
    <xsl:apply-templates />
    <span class='visuallyHidden'>End of error</span>
  </div>
</xsl:template>

<xsl:template match="w:tbl[w:tblPr/w:tblStyle/@w:val='Callout-Example']">
  <div class='example'>
    <xsl:apply-templates />
    <span class='visuallyHidden'>End of example</span>
  </div>
</xsl:template>


<!-- ========================================================================================== -->
<!-- TABLES                                                                                     -->
<!-- ========================================================================================== -->

<!--tables-->
<xsl:template match="w:tbl[w:tblPr/w:tblStyle/@w:val='Tablewithborder' or w:tblPr/w:tblStyle/@w:val='Tablewithoutborder' or w:tblPr/w:tblStyle/@w:val='Tableindent1' or w:tblPr/w:tblStyle/@w:val='Tableindent2' or w:tblPr/w:tblStyle/@w:val='Tableindent3']">
    <!--if either of previous 2 siblings is a paragraph with Tablecaption or Tablesummary paragraph style-->
    <table>
        <!--Apply tblNoBorder class to table without border-->
        <xsl:if test="./w:tblPr/w:tblStyle/@w:val='Tablewithoutborder'">
            <xsl:attribute name="class">tblNoBorder</xsl:attribute>
        </xsl:if>

        <xsl:if test="./w:tblPr/w:tblStyle/@w:val='Tableindent1'">
            <xsl:attribute name="class">indent1</xsl:attribute>
        </xsl:if>

        <xsl:if test="./w:tblPr/w:tblStyle/@w:val='Tableindent2'">
            <xsl:attribute name="class">indent2</xsl:attribute>
        </xsl:if>

        <xsl:if test="./w:tblPr/w:tblStyle/@w:val='Tableindent3'">
            <xsl:attribute name="class">indent3</xsl:attribute>
        </xsl:if>

        <xsl:choose>
            <!--if first preceding sibling is styled Tablecaption, there is no summary, write caption-->
            <xsl:when test="preceding-sibling::w:p[1][./w:pPr/w:pStyle/@w:val='Tablecaption']">
                <caption>
                    <xsl:apply-templates select ="preceding-sibling::w:p[1]/child::*" />
                </caption>
            </xsl:when>
            <xsl:otherwise>
                <!--if first preceding sibling is style Tablesummary and second preceding is Tablecaption write both-->
                <xsl:if test="preceding-sibling::w:p[1][./w:pPr/w:pStyle/@w:val='Tablesummary'] and preceding-sibling::w:p[2][./w:pPr/w:pStyle/@w:val='Tablecaption']">
                    <xsl:attribute name="summary">
                        <xsl:apply-templates select ="preceding-sibling::w:p[1]/child::*" />
                    </xsl:attribute>

                    <caption>
                        <xsl:apply-templates select ="preceding-sibling::w:p[2]/child::*" />
                    </caption>
                </xsl:if>
                <!--if first preceeding sibling is style Tablesummary and second preceding is not Tablecaption, just write summary-->
                <xsl:if test="preceding-sibling::w:p[1][./w:pPr/w:pStyle/@w:val='Tablesummary'] and preceding-sibling::w:p[2][./w:pPr/w:pStyle/@w:val!='Tablecaption']">
                    <xsl:attribute name="summary">
                        <xsl:apply-templates select ="preceding-sibling::w:p[1]/child::*" />
                    </xsl:attribute>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:for-each select="w:tr">
            <tr>
                <xsl:for-each select="w:tc">
                                            <!--ANDREW BIRCH'S CONTRIBUTION - Merged Rows & Merged Columns-->
                    <!-- XXX -->
                    <!-- Variable: Stores the number of columns that the currently selected merged cell is from the left (needed for the next function to calcuate the number of rows down a merged cell goes-->
                    <xsl:variable name="NumberOfColumnsAcross" select="0">
<!--                         <xsl:choose> -->
<!--                             <xsl:when test="user:IsStartOfCellMergedVertically(.) = true()"> -->
<!--                                 <xsl:value-of select="user:CountColumnsAcrossForCurrentCell(node())"/> -->
<!--                             </xsl:when> -->
<!--                             <xsl:otherwise>0</xsl:otherwise> -->
<!--                         </xsl:choose> -->
                    </xsl:variable>

                    <!-- Variable: Stores the number of rows down the currently selected merged cell goes down, so appropriate rowspan can be returned-->
                    <xsl:variable name="NumberOfRowsDown" select="0">
<!--                         <xsl:choose> -->
<!--                             <xsl:when test="user:IsStartOfCellMergedVertically(.)"> -->
<!--                                 <xsl:value-of select="user:CountNumberOfMergedRowsDown(../node(), $NumberOfColumnsAcross)" /> -->
<!--                             </xsl:when> -->
<!--                             <xsl:otherwise>0</xsl:otherwise> -->
<!--                         </xsl:choose> -->
                    </xsl:variable>

                    <!-- If the current cell property is a w:vmerge w:val = restart, then proceed with checking if the number of rows down is appropriate for a Rowspan attribute-->

                    <xsl:choose>
                        <xsl:when test="descendant-or-self::w:tcPr/w:vmerge[not(@*)]">
                        </xsl:when>
                        <xsl:otherwise>
                         <xsl:variable name="colspan">
                                <xsl:choose>
                                    <xsl:when test="./w:tcPr/w:gridSpan/@w:val>1">
                                        <xsl:value-of select="./w:tcPr/w:gridSpan/@w:val"/>
                                    </xsl:when>
                                    <xsl:otherwise></xsl:otherwise>
                                </xsl:choose>
                            </xsl:variable>
                            <xsl:variable name="rowspan">
                                <xsl:choose>
                                    <xsl:when test="$NumberOfRowsDown > 1">
                                        <xsl:value-of select="$NumberOfRowsDown"/>
                                    </xsl:when>
                                    <xsl:otherwise></xsl:otherwise>
                                </xsl:choose>
                            </xsl:variable>
                            <xsl:choose>
                                <!--If current cell has shading, write th, otherwise td-->
                                <xsl:when test="./w:p/w:pPr/w:pStyle/@w:val='Tableheading'">
                                    <xsl:element name="th">
                                        <xsl:if test="./w:tcPr/w:gridSpan/@w:val>1">
                                            <xsl:attribute name="colspan"><xsl:value-of select="$colspan"/></xsl:attribute>
                                        </xsl:if>
                                        <xsl:if test="$NumberOfRowsDown > 1">
                                            <xsl:attribute name="rowspan"><xsl:value-of select="$NumberOfRowsDown"/></xsl:attribute>
                                        </xsl:if>
                                        <xsl:apply-templates />
                                        <xsl:if test ="count(descendant::w:r)=0">&#160;</xsl:if>
                                    </xsl:element>
                                </xsl:when>
                                <xsl:when test="./w:p/w:pPr/w:pStyle/@w:val='Tableheadingright'">
                                    <xsl:element name="th">
                                        <xsl:attribute name="class">alignRight</xsl:attribute>
                                        <xsl:if test="./w:tcPr/w:gridSpan/@w:val>1">
                                            <xsl:attribute name="colspan"><xsl:value-of select="$colspan"/></xsl:attribute>
                                        </xsl:if>
                                        <xsl:if test="$NumberOfRowsDown > 1">
                                            <xsl:attribute name="rowspan"><xsl:value-of select="$NumberOfRowsDown"/></xsl:attribute>
                                        </xsl:if>
                                        <xsl:apply-templates />
                                        <xsl:if test ="count(descendant::w:r)=0">&#160;</xsl:if>
                                    </xsl:element>
                                </xsl:when>
                                <xsl:when test="./w:p/w:pPr/w:pStyle/@w:val='Tableheadingcentre'">
                                    <xsl:element name="th">
                                        <xsl:attribute name="class">alignCentre</xsl:attribute>
                                        <xsl:if test="./w:tcPr/w:gridSpan/@w:val>1">
                                            <xsl:attribute name="colspan"><xsl:value-of select="$colspan"/></xsl:attribute>
                                        </xsl:if>
                                        <xsl:if test="$NumberOfRowsDown > 1">
                                            <xsl:attribute name="rowspan"><xsl:value-of select="$NumberOfRowsDown"/></xsl:attribute>
                                        </xsl:if>
                                        <xsl:apply-templates />
                                        <xsl:if test ="count(descendant::w:r)=0">&#160;</xsl:if>
                                    </xsl:element>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:element name="td">
                                        <xsl:if test="./w:tcPr/w:gridSpan/@w:val>1">
                                            <xsl:attribute name="colspan"><xsl:value-of select="$colspan"/></xsl:attribute>
                                        </xsl:if>
                                        <xsl:if test="$NumberOfRowsDown > 1">
                                            <xsl:attribute name="rowspan"><xsl:value-of select="$rowspan"/></xsl:attribute>
                                        </xsl:if>
                                        <xsl:apply-templates />
                                        <xsl:if test ="count(descendant::w:r)=0">&#160;</xsl:if>
                                    </xsl:element>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
                    <!--End ANDREW BIRCH'S CONTRIBUTION - Merged Rows & Merged Columns-->
                </xsl:for-each>
            </tr>
        </xsl:for-each>
    </table>
</xsl:template>
<!--tables-->

<!-- Tablecaption and Tablesummary - written in tables so can be ignored -->
<xsl:template match="w:p[w:pPr/w:pStyle/@w:val='Tablecaption']" priority ="2" />
<xsl:template match="w:p[w:pPr/w:pStyle/@w:val='Tablesummary']" priority ="2" />


<!-- ========================================================================================== -->
<!-- CHARACTER STYLES                                                                           -->
<!-- ========================================================================================== -->

<!--Handle all hyperlink that is not after any runs with that same style-->
<xsl:template match="w:hlink" priority="2">
  <xsl:variable name="previousNode" select = "./preceding-sibling::*[self::w:hlink|self::w:r][1]" />
  <xsl:choose>
    <xsl:when test ="w:r/w:rPr/w:rStyle/@w:val='Link-External' and
              (not($previousNode[self::w:r]/w:rPr/w:rStyle/@w:val = 'Link-External') or
              not($previousNode[self::hlink]/w:r/w:rPr/w:rStyle/@w:val = 'Link-External'))">
        <xsl:element name="a">
            <xsl:attribute name="href">
                <xsl:call-template name="extractHyperlink">
                    <xsl:with-param name="currentNode" select ="." />
                </xsl:call-template>
            </xsl:attribute>
            <xsl:attribute name="class">external</xsl:attribute>
            <!--Recursively add text of consecutive runs of the hyperlink-->
            <xsl:call-template name="concatSameStyleRun">
                <xsl:with-param name="currentNode" select ="./w:r" />
            </xsl:call-template>
            <span>External Link</span>
        </xsl:element>
    </xsl:when>
    <xsl:when test ="w:r/w:rPr/w:rStyle/@w:val='Link-Internal' and
              (not($previousNode[self::w:r]/w:rPr/w:rStyle/@w:val = 'Link-Internal') or
              not($previousNode[self::hlink]/w:r/w:rPr/w:rStyle/@w:val = 'Link-Internal'))">
        <xsl:element name="a">
            <xsl:attribute name="href">
                <xsl:call-template name="extractHyperlink">
                    <xsl:with-param name="currentNode" select ="." />
                </xsl:call-template>
            </xsl:attribute>
            <!--Recursively add text of consecutive runs of the hyperlink-->
            <xsl:call-template name="concatSameStyleRun">
                <xsl:with-param name="currentNode" select ="./w:r" />
            </xsl:call-template>
        </xsl:element>
    </xsl:when>


    <xsl:when test ="w:r/w:rPr/w:rStyle/@w:val='Link-Internalbold' and
              (not($previousNode[self::w:r]/w:rPr/w:rStyle/@w:val = 'Link-Internalbold') or
              not($previousNode[self::hlink]/w:r/w:rPr/w:rStyle/@w:val = 'Link-Internalbold'))">
        <xsl:element name="a">
            <xsl:attribute name="href">
                <xsl:call-template name="extractHyperlink">
                    <xsl:with-param name="currentNode" select ="." />
                </xsl:call-template>
            </xsl:attribute>
            <!--Recursively add text of consecutive runs of the hyperlink-->
            <strong>
                <xsl:call-template name="concatSameStyleRun">
                    <xsl:with-param name="currentNode" select ="./w:r" />
                </xsl:call-template>
            </strong>
        </xsl:element>
    </xsl:when>
    <xsl:when test ="w:r/w:rPr/w:rStyle/@w:val='Link-Internalitalics' and
              (not($previousNode[self::w:r]/w:rPr/w:rStyle/@w:val = 'Link-Internalitalics') or
              not($previousNode[self::hlink]/w:r/w:rPr/w:rStyle/@w:val = 'Link-Internalitalics'))">
        <xsl:element name="a">
            <xsl:attribute name="href">
                <xsl:call-template name="extractHyperlink">
                    <xsl:with-param name="currentNode" select ="." />
                </xsl:call-template>
            </xsl:attribute>
            <!--Recursively add text of consecutive runs of the hyperlink-->
            <em>
                <xsl:call-template name="concatSameStyleRun">
                    <xsl:with-param name="currentNode" select ="./w:r" />
                </xsl:call-template>
            </em>
        </xsl:element>
    </xsl:when>


    <xsl:when test ="w:r/w:rPr/w:rStyle/@w:val='Link-Newwindow' and
              (not($previousNode[self::w:r]/w:rPr/w:rStyle/@w:val = 'Link-Newwindow') or
              not($previousNode[self::hlink]/w:r/w:rPr/w:rStyle/@w:val = 'Link-Newwindow'))">
        <xsl:element name="a">
            <xsl:attribute name="href">
                <xsl:call-template name="extractHyperlink">
                    <xsl:with-param name="currentNode" select ="." />
                </xsl:call-template>
            </xsl:attribute>
            <xsl:attribute name="class">newWindow</xsl:attribute>
            <xsl:attribute name="target">_blank</xsl:attribute>
            <!--Recursively add text of consecutive runs of the hyperlink-->
            <xsl:call-template name="concatSameStyleRun">
                <xsl:with-param name="currentNode" select ="./w:r" />
            </xsl:call-template>
            <span>This link opens in a new window</span>
        </xsl:element>
    </xsl:when>
    <xsl:when test ="w:r/w:rPr/w:rStyle/@w:val='Link-Bookmark' and
              (not($previousNode[self::w:r]/w:rPr/w:rStyle/@w:val = 'Link-Bookmark') or
              not($previousNode[self::hlink]/w:r/w:rPr/w:rStyle/@w:val = 'Link-Bookmark')) ">
        <xsl:element name="a">
            <xsl:attribute name="href">
                <xsl:call-template name="extractHyperlink">
                    <xsl:with-param name="currentNode" select ="." />
                </xsl:call-template>
            </xsl:attribute>
            <xsl:attribute name="class">bookmark</xsl:attribute>
            <!--Recursively add text of consecutive runs of the hyperlink-->
            <xsl:call-template name="concatSameStyleRun">
                <xsl:with-param name="currentNode" select ="./w:r" />
            </xsl:call-template>
        </xsl:element>
    </xsl:when>
    <xsl:when test ="w:r/w:rPr/w:rStyle/@w:val='Link-Footnote' and
              (not($previousNode[self::w:r]/w:rPr/w:rStyle/@w:val = 'Link-Footnote') or
              not($previousNode[self::hlink]/w:r/w:rPr/w:rStyle/@w:val = 'Link-Footnote')) ">
        <sup>
            <xsl:element name="a">
                <xsl:attribute name="href">
                    <xsl:call-template name="extractHyperlink">
                        <xsl:with-param name="currentNode" select ="." />
                    </xsl:call-template>
                </xsl:attribute>
                <!--Recursively add text of consecutive runs of the hyperlink-->
                <xsl:call-template name="concatSameStyleRun">
                    <xsl:with-param name="currentNode" select ="./w:r" />
                </xsl:call-template>
            </xsl:element>
        </sup>
    </xsl:when>
    <xsl:when test ="./w:r/w:rPr/w:rStyle/@w:val='StyleBold' and
              (not($previousNode[self::w:r]/w:rPr/w:rStyle/@w:val = 'StyleBold') or
              not($previousNode[self::hlink]/w:r/w:rPr/w:rStyle/@w:val = 'StyleBold'))">
        <strong>
            <xsl:call-template name="concatSameStyleRun">
                <xsl:with-param name="currentNode" select ="."></xsl:with-param>
            </xsl:call-template>
        </strong>
    </xsl:when>
    <xsl:when test ="./w:r/w:rPr/w:rStyle/@w:val='StyleBoldItalic' and
              (not($previousNode[self::w:r]/w:rPr/w:rStyle/@w:val = 'StyleBoldItalic') or
              not($previousNode[self::hlink]/w:r/w:rPr/w:rStyle/@w:val = 'StyleBoldItalic'))">
        <strong>
            <em>
                <xsl:call-template name="concatSameStyleRun">
                    <xsl:with-param name="currentNode" select ="."></xsl:with-param>
                </xsl:call-template>
            </em>
        </strong>
    </xsl:when>
    <xsl:when test ="./w:r/w:rPr/w:rStyle/@w:val='Superscript' and
              (not($previousNode[self::w:r]/w:rPr/w:rStyle/@w:val = 'Superscript') or
              not($previousNode[self::hlink]/w:r/w:rPr/w:rStyle/@w:val = 'Superscript'))">
        <sup>
            <xsl:call-template name="concatSameStyleRun">
                <xsl:with-param name="currentNode" select ="."></xsl:with-param>
            </xsl:call-template>
        </sup>
    </xsl:when>
    <xsl:when test ="./w:r/w:rPr/w:rStyle/@w:val='StyleItalic' and
              (not($previousNode[self::w:r]/w:rPr/w:rStyle/@w:val = 'StyleItalic') or
              not($previousNode[self::hlink]/w:r/w:rPr/w:rStyle/@w:val = 'StyleItalic'))">
        <em>
            <xsl:call-template name="concatSameStyleRun">
                <xsl:with-param name="currentNode" select ="."></xsl:with-param>
            </xsl:call-template>
        </em>
    </xsl:when>
    <xsl:when test ="./w:r/w:rPr/w:rStyle/@w:val='StyleUnderline' and
              (not($previousNode[self::w:r]/w:rPr/w:rStyle/@w:val = 'StyleUnderline') or
              not($previousNode[self::hlink]/w:r/w:rPr/w:rStyle/@w:val = 'StyleUnderline'))">
        <u>
            <xsl:call-template name="concatSameStyleRun">
                <xsl:with-param name="currentNode" select ="."></xsl:with-param>
            </xsl:call-template>
        </u>
    </xsl:when>
    <xsl:when test ="./w:r/w:rPr/w:rStyle/@w:val='Nobreak' and
              (not($previousNode[self::w:r]/w:rPr/w:rStyle/@w:val = 'Nobreak') or
              not($previousNode[self::hlink]/w:r/w:rPr/w:rStyle/@w:val = 'Nobreak'))">
        <span class="nobreak">
            <xsl:call-template name="concatSameStyleRun">
                <xsl:with-param name="currentNode" select ="."></xsl:with-param>
            </xsl:call-template>
        </span>
    </xsl:when>
    <xsl:otherwise>
        <xsl:if test="not(($previousNode[self::w:r][not(w:rPr/w:rStyle/@w:val)]  or $previousNode[self::w:hlink]/w:r/w:rPr/w:rStyle[@w:val='Hyperlink']) and
               (self::w:hlink/w:r/w:rPr/w:rStyle[@w:val='Hyperlink'] or self::w:r[not(w:rPr/w:rStyle/@w:val)]))">
            <xsl:call-template name="concatSameStyleRun">
                <xsl:with-param name="currentNode" select ="."></xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!--Generates a standard anchor link-->
<xsl:template name="generateStandardLink">
  <a>
    <xsl:attribute name="href">
        <xsl:call-template name="extractHyperlink">
            <xsl:with-param name="currentNode" select ="." />
        </xsl:call-template>
    </xsl:attribute>
    <!--Recursively add text of consecutive runs of the hyperlink-->
    <xsl:call-template name="concatSameStyleRun">
        <xsl:with-param name="currentNode" select ="./w:r" />
    </xsl:call-template>
  </a>
</xsl:template>

<!--Extracts the href from the hlink and determines if it's a bookmark-->
<xsl:template name="extractHyperlink">
  <xsl:param name="currentNode"></xsl:param>
  <xsl:choose>
    <xsl:when test ="$currentNode[@w:bookmark]">
      <xsl:text>#</xsl:text>
      <xsl:value-of select="$currentNode/@w:bookmark"/>
    </xsl:when>
    <xsl:when test ="$currentNode[@w:dest]">
      <xsl:value-of select="$currentNode/@w:dest"/>
    </xsl:when>
  </xsl:choose>
</xsl:template>

<!--Ensure that all word runs are not handled by the standard w:r template
as these runs are handled by the hlink template-->
<xsl:template match="w:hlink/w:r" priority="2" />

<!--Handles all word runs-->
<xsl:template match="w:r" priority="1">
  <xsl:variable name="previousNode" select = "./preceding-sibling::*[self::w:hlink|self::w:r][1]" />
  <xsl:variable name="nextNode" select = "./following-sibling::*[self::w:hlink|self::w:r][1]" />
  <xsl:choose>
    <!-- All  items thats already processed in the first item section are ignored -->
    <xsl:when test="$previousNode[self::w:r]/w:rPr/w:rStyle/@w:val = ./w:rPr/w:rStyle/@w:val or
              $previousNode[self::w:hlink]/w:r/w:rPr/w:rStyle/@w:val = ./w:rPr/w:rStyle/@w:val or
               (($previousNode[self::w:r][not(w:rPr/w:rStyle/@w:val)]  or $previousNode[self::w:hlink]/w:r/w:rPr/w:rStyle[@w:val='Hyperlink']) and
               (self::w:hlink/w:r/w:rPr/w:rStyle[@w:val='Hyperlink'] or self::w:r[not(w:rPr/w:rStyle/@w:val)])) " />
    <xsl:otherwise>
      <xsl:choose>
        <!-- XXX -->
<!--         <xsl:when test ="count(preceding::aml:annotation[@w:type='Word.Bookmark.Start'])>count(preceding::aml:annotation[@w:type='Word.Bookmark.End']) and -->
<!--                   (count(descendant::w:t)>0 or  -->
<!--                    $nextNode[self::w:hlink]/w:r/w:rPr/w:rStyle/@w:val = ./w:rPr/w:rStyle/@w:val or  -->
<!--                    $nextNode[self::w:r]/w:rPr/w:rStyle/@w:val = ./w:rPr/w:rStyle/@w:val)"> -->
        <xsl:when test ="(count(descendant::w:t)>0 or
                   $nextNode[self::w:hlink]/w:r/w:rPr/w:rStyle/@w:val = ./w:rPr/w:rStyle/@w:val or
                   $nextNode[self::w:r]/w:rPr/w:rStyle/@w:val = ./w:rPr/w:rStyle/@w:val)">
            <a class="anchor">
            <!-- XXX -->
<!--                 <xsl:attribute name ="id"> -->
<!--                     <xsl:value-of select ="preceding::aml:annotation[@w:type='Word.Bookmark.Start'][1]/@w:name" /> -->
<!--                 </xsl:attribute> -->
                <xsl:call-template name="generateRunTags"/>
            </a>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="generateRunTags"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!--Generates the tag for the run and concatenate following runs of the same style-->
<xsl:template name="generateRunTags" >
    <xsl:choose>
        <xsl:when test ="./w:rPr/w:rStyle/@w:val='StyleBold'">
            <strong>
                <xsl:call-template name="concatSameStyleRun">
                    <xsl:with-param name="currentNode" select ="."></xsl:with-param>
                </xsl:call-template>
            </strong>
        </xsl:when>
        <xsl:when test ="./w:rPr/w:rStyle/@w:val='StyleBoldItalic'">
            <strong>
                <em>
                    <xsl:call-template name="concatSameStyleRun">
                        <xsl:with-param name="currentNode" select ="."></xsl:with-param>
                    </xsl:call-template>
                </em>
            </strong>
        </xsl:when>
        <xsl:when test ="./w:rPr/w:rStyle/@w:val='Superscript'">
            <sup>
                <xsl:call-template name="concatSameStyleRun">
                    <xsl:with-param name="currentNode" select ="."></xsl:with-param>
                </xsl:call-template>
            </sup>
        </xsl:when>
        <xsl:when test ="./w:rPr/w:rStyle/@w:val='StyleItalic'">
            <em>
                <xsl:call-template name="concatSameStyleRun">
                    <xsl:with-param name="currentNode" select ="."></xsl:with-param>
                </xsl:call-template>
            </em>
        </xsl:when>
        <xsl:when test ="./w:rPr/w:rStyle/@w:val='StyleUnderline'">
            <u>
                <xsl:call-template name="concatSameStyleRun">
                    <xsl:with-param name="currentNode" select ="."></xsl:with-param>
                </xsl:call-template>
            </u>
        </xsl:when>
        <xsl:when test ="./w:rPr/w:rStyle/@w:val='Nobreak'">
            <span class="nobreak">
                <xsl:call-template name="concatSameStyleRun">
                    <xsl:with-param name="currentNode" select ="."></xsl:with-param>
                </xsl:call-template>
            </span>
        </xsl:when>
        <xsl:when test ="./w:rPr/w:rStyle/@w:val='Link-Internal'">
            <xsl:if test="./w:instrText|following-sibling::w:r[./w:instrText][./w:rPr/w:rStyle/@w:val='Link-Internal']/w:instrText">
                <a>
                    <xsl:attribute name="href">
                        <xsl:call-template name="extractRunHyperlink">
                            <xsl:with-param name="currentNode" select ="./w:instrText|following-sibling::w:r[./w:instrText]/w:instrText" />
                        </xsl:call-template>
                    </xsl:attribute>
                    <!--Recursively add text of consecutive runs of the hyperlink-->
                    <xsl:call-template name="concatSameStyleRun">
                        <xsl:with-param name="currentNode" select ="." />
                    </xsl:call-template>
                </a>
            </xsl:if>
        </xsl:when>

        <xsl:when test ="./w:rPr/w:rStyle/@w:val='Link-Internalbold'">
            <xsl:if test="./w:instrText|following-sibling::w:r[./w:instrText][./w:rPr/w:rStyle/@w:val='Link-Internalbold']/w:instrText">
                <a>
                    <xsl:attribute name="href">
                        <xsl:call-template name="extractRunHyperlink">
                            <xsl:with-param name="currentNode" select ="./w:instrText|following-sibling::w:r[./w:instrText]/w:instrText" />
                        </xsl:call-template>
                    </xsl:attribute>
                    <!--Recursively add text of consecutive runs of the hyperlink-->
                    <strong>
                        <xsl:call-template name="concatSameStyleRun">
                            <xsl:with-param name="currentNode" select ="." />
                        </xsl:call-template>
                    </strong>
                </a>
            </xsl:if>
        </xsl:when>
        <xsl:when test ="./w:rPr/w:rStyle/@w:val='Link-Internalitalics'">
            <xsl:if test="./w:instrText|following-sibling::w:r[./w:instrText][./w:rPr/w:rStyle/@w:val='Link-Internalitalics']/w:instrText">
                <a>
                    <xsl:attribute name="href">
                        <xsl:call-template name="extractRunHyperlink">
                            <xsl:with-param name="currentNode" select ="./w:instrText|following-sibling::w:r[./w:instrText]/w:instrText" />
                        </xsl:call-template>
                    </xsl:attribute>
                    <!--Recursively add text of consecutive runs of the hyperlink-->
                    <em>
                        <xsl:call-template name="concatSameStyleRun">
                            <xsl:with-param name="currentNode" select ="." />
                        </xsl:call-template>
                    </em>
                </a>
            </xsl:if>
        </xsl:when>

        <xsl:when test ="./w:rPr/w:rStyle/@w:val='Link-External'">
            <xsl:if test="./w:instrText|following-sibling::w:r[./w:instrText][./w:rPr/w:rStyle/@w:val='Link-External']/w:instrText">
                <a>
                    <xsl:attribute name="href">
                        <xsl:call-template name="extractRunHyperlink">
                            <xsl:with-param name="currentNode" select ="./w:instrText|following-sibling::w:r[./w:instrText]/w:instrText" />
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="class">external</xsl:attribute>
                    <!--Recursively add text of consecutive runs of the hyperlink-->
                    <xsl:call-template name="concatSameStyleRun">
                        <xsl:with-param name="currentNode" select ="." />
                    </xsl:call-template>
                    <span>External Link</span>
                </a>
            </xsl:if>
        </xsl:when>
        <xsl:when test ="./w:rPr/w:rStyle/@w:val='Link-Newwindow'">
            <xsl:if test="./w:instrText|following-sibling::w:r[./w:instrText][./w:rPr/w:rStyle/@w:val='Link-Newwindow']/w:instrText">
                <a>
                    <xsl:attribute name="href">
                        <xsl:call-template name="extractRunHyperlink">
                            <xsl:with-param name="currentNode" select ="./w:instrText|following-sibling::w:r[./w:instrText]/w:instrText" />
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="class">newWindow</xsl:attribute>
                    <xsl:attribute name="target">_blank</xsl:attribute>
                    <!--Recursively add text of consecutive runs of the hyperlink-->
                    <xsl:call-template name="concatSameStyleRun">
                        <xsl:with-param name="currentNode" select ="." />
                    </xsl:call-template>
                    <span>This link opens in a new window</span>
                </a>
            </xsl:if>
        </xsl:when>
        <xsl:when test ="./w:rPr/w:rStyle/@w:val='Link-Bookmark'">
            <xsl:if test="./w:instrText|following-sibling::w:r[./w:instrText][./w:rPr/w:rStyle/@w:val='Link-Bookmark']/w:instrText">
                <a>
                    <xsl:attribute name="href">
                        <xsl:call-template name="extractRunHyperlink">
                            <xsl:with-param name="currentNode" select ="./w:instrText|following-sibling::w:r[./w:instrText]/w:instrText" />
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="class">bookmark</xsl:attribute>
                    <!--Recursively add text of consecutive runs of the hyperlink-->
                    <xsl:call-template name="concatSameStyleRun">
                        <xsl:with-param name="currentNode" select ="." />
                    </xsl:call-template>
                </a>
            </xsl:if>
        </xsl:when>
        <xsl:when test ="./w:rPr/w:rStyle/@w:val='Link-Footnote'">
            <xsl:if test="./w:instrText|following-sibling::w:r[./w:instrText][./w:rPr/w:rStyle/@w:val='Link-Footnote']/w:instrText">
                <a>
                    <xsl:attribute name="href">
                        <xsl:call-template name="extractRunHyperlink">
                            <xsl:with-param name="currentNode" select ="./w:instrText|following-sibling::w:r[./w:instrText]/w:instrText" />
                        </xsl:call-template>
                    </xsl:attribute>
                    <!--Recursively add text of consecutive runs of the hyperlink-->
                    <xsl:call-template name="concatSameStyleRun">
                        <xsl:with-param name="currentNode" select ="." />
                    </xsl:call-template>
                </a>
            </xsl:if>
        </xsl:when>
        <xsl:otherwise>
            <xsl:call-template name="concatSameStyleRun">
                <xsl:with-param name="currentNode" select ="."></xsl:with-param>
            </xsl:call-template>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<!--Recursively add text of consecutive runs with the same style-->
<xsl:template name="concatSameStyleRun">
    <xsl:param name="currentNode"></xsl:param>
    <xsl:choose>
        <xsl:when test="$currentNode[1][self::w:r]">
            <xsl:for-each select ="$currentNode[1]/child::*">
                <xsl:choose>
                    <xsl:when test ="self::w:br[not(@w:type)]">
                        <br/>
                    </xsl:when>
                    <xsl:when test ="self::w:t">
                        <xsl:value-of select="./text()"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:for-each>
            <!--Concat runs if they are of the same styles-->
            <xsl:variable name="nextNode" select = "$currentNode/following-sibling::*[self::w:hlink|self::w:r][1]" />
            <xsl:if test="$nextNode[self::w:r]/w:rPr/w:rStyle/@w:val = $currentNode/w:rPr/w:rStyle/@w:val or
                          $nextNode[self::w:hlink]/w:r/w:rPr/w:rStyle/@w:val = $currentNode/w:rPr/w:rStyle/@w:val or
                    (($nextNode[self::w:r][not(w:rPr/w:rStyle/@w:val)]  or $nextNode[self::w:hlink]/w:r/w:rPr/w:rStyle[@w:val='Hyperlink'])
                      and $currentNode[not(w:rPr/w:rStyle/@w:val)])">
                <xsl:call-template name="concatSameStyleRun">
                    <xsl:with-param name="currentNode" select ="$nextNode" />
                </xsl:call-template>
            </xsl:if>
        </xsl:when>
        <xsl:when test="$currentNode[1][self::w:hlink]">
            <xsl:element name="a">
                <xsl:attribute name="href">
                    <xsl:call-template name="extractHyperlink">
                        <xsl:with-param name="currentNode" select ="$currentNode" />
                    </xsl:call-template>
                </xsl:attribute>
                <!--Recursively add text of consecutive runs of the hyperlink-->
                <xsl:call-template name="concatSameStyleRun">
                    <xsl:with-param name="currentNode" select ="$currentNode/w:r" />
                </xsl:call-template>
            </xsl:element>
            <!--Concat runs if they are of the same styles-->
            <xsl:variable name="nextNode" select = "$currentNode/following-sibling::*[self::w:hlink|self::w:r][1]" />
            <xsl:if test="$nextNode[self::w:r]/w:rPr/w:rStyle/@w:val = $currentNode/w:r/w:rPr/w:rStyle/@w:val or
                          $nextNode[self::w:hlink]/w:r/w:rPr/w:rStyle/@w:val = $currentNode/w:r/w:rPr/w:rStyle/@w:val or
                          (($nextNode[self::w:r][not(w:rPr/w:rStyle/@w:val)]  or $nextNode[self::w:hlink]/w:r/w:rPr/w:rStyle[@w:val='Hyperlink'])
                          and $currentNode[self::w:hlink]/w:r/w:rPr/w:rStyle[@w:val='Hyperlink'])">
                <xsl:call-template name="concatSameStyleRun">
                    <xsl:with-param name="currentNode" select ="$nextNode" />
                </xsl:call-template>
            </xsl:if>
        </xsl:when>
    </xsl:choose>
</xsl:template>

<!--Extracts the URL from the hyperlink instruction text by removing
    'HYPERLINK' and quotes from the instruction text
    Hyperlinks with \l requires an additional # for local anchors/bookmark-->
<xsl:template name="extractRunHyperlink">
    <xsl:param name="currentNode"></xsl:param>
    <xsl:variable name="hyperlink" select="translate($currentNode[self::w:instrText]/text(),' ','')"></xsl:variable>
    <xsl:choose>
        <xsl:when test="(string-length(substring-after(
            $hyperlink, 'HYPERLINK\l')) > 0)">
            <xsl:text>#</xsl:text>
            <xsl:value-of select="substring-before(substring-after($hyperlink, 'HYPERLINK\l&quot;'), '&quot;')" />
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="substring-before(substring-after($hyperlink, '&quot;'), '&quot;')" />
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<!-- ##End Character Styles##-->

<!--Paragraphs in Section Properties are ignored as they are for headers, footers, etc-->
<xsl:template match="w:sectPr" priority ="2" />

<!--Ignore unhandled text nodes-->
<xsl:template match="text()" />

</xsl:stylesheet>
