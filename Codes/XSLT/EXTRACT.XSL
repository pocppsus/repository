<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n1="urn:hl7-org:v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <xsl:output method="html" indent="yes" version="4.01" encoding="ISO-8859-1" doctype-system="http://www.w3.org/TR/html4/strict.dtd" doctype-public="-//W3C//DTD HTML 4.01//EN"/>
   <xsl:variable name="title">
      <xsl:choose>
         <xsl:when test="string-length(EHR_EXTRACT/all_compositions/name/originalText)  &gt;= 1">
            <xsl:value-of select="EHR_EXTRACT/all_compositions/name/originalText"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:text>Extrato</xsl:text>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:template match="/">
      <xsl:apply-templates select="EHR_EXTRACT"/>
   </xsl:template>
   <!-- produce browser rendered, human readable clinical document -->
   <xsl:template match="EHR_EXTRACT">
      <html>
         <head>
            <xsl:comment> Do NOT edit this HTML directly: it was generated via an XSLT transformation from a CDA Release 2 XML document. </xsl:comment>
            <title>
               <xsl:value-of select="$title"/>
            </title>
			<xsl:call-template name="addCSS"/>
         </head>
         <body>
            <h1 class="h1center">
               <xsl:value-of select="$title"/>
            </h1>
			<xsl:if test="count(all_compositions/content) &gt; 0">
                <xsl:call-template name="tableofcontents"/>
				<div class="content" name="root">
					<xsl:call-template name="elementIterator">
						<xsl:with-param name="iterator" select="all_compositions/content"/>						
					</xsl:call-template>
					
				</div>
            </xsl:if>
            
         </body>
      </html>
   </xsl:template>
   <!-- generate table of contents -->
   <xsl:template name="tableofcontents">
		<div class="middleNav">
            <ul class="tabs">
					<li>
						<a href="#root"  data-tipsyon="true">
							<span>Todos elementos</span>
						</a>
					</li>			
				<xsl:for-each select="all_compositions/content">
					<li>
						<a href="#{generate-id(name/originalText)}"  data-tipsyon="true">
							<span><xsl:value-of select="name/originalText"/></span>
						</a>
					</li>
				</xsl:for-each>
				
			</ul>
		</div>			
   </xsl:template>
   <!-- iterator of nested elements  -->
   <xsl:template name="elementIterator">
		<xsl:param name="iterator"/>
		<xsl:for-each select="$iterator">
			<xsl:call-template name="chooseElementType">
				<xsl:with-param name="chooseElementTitle" select="name/originalText"/>
			</xsl:call-template>
		</xsl:for-each>
   </xsl:template>
   <!-- element xsi type ou select for choose format layout -->
   <xsl:template name="chooseElementType">
		<xsl:param name="chooseElementTitle"/>
		<xsl:choose>
			<xsl:when test="contains(@xsi:type,'SECTION')">
				<xsl:call-template name="section">
					<xsl:with-param name="sectionTitle" select="$chooseElementTitle"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="contains(@xsi:type,'ENTRY')">
				<xsl:call-template name="entry">
					<xsl:with-param name="entryTitle" select="$chooseElementTitle"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="contains(@xsi:type,'CLUSTER')">
				<xsl:call-template name="cluster">
					<xsl:with-param name="clusterTitle" select="$chooseElementTitle"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="contains(@xsi:type,'ELEMENT')">
				<xsl:call-template name="chooseDataTypeFormat"/>
			</xsl:when>
		</xsl:choose>
   </xsl:template>   
   <!-- The section layout-->
   <xsl:template name="section">
		<xsl:param name="sectionTitle"/>
		<div class="section" name="{generate-id($sectionTitle)}" style="width: 100%;" align="left">
			<fieldset class="fieldset">			
				<legend class="fieldset-legend"><xsl:value-of select="$sectionTitle"/></legend>
				<div style="width: 100%;" align="left">
					<xsl:call-template name="elementIterator">
						<xsl:with-param name="iterator" select="members"/>						
					</xsl:call-template>
				</div>
			</fieldset>
		</div>
   </xsl:template>
   <!-- The entry layout-->   
   <xsl:template name="entry">
		<xsl:param name="entryTitle"/>
		<div class="entry" name="{generate-id($entryTitle)}" style="width: 100%;" align="left">
			<fieldset class="fieldset">			
				<legend class="fieldset-legend"><xsl:value-of select="$entryTitle"/></legend>
				<div style="width: 100%;" align="left">
					<xsl:call-template name="elementIterator">
						<xsl:with-param name="iterator" select="items"/>						
					</xsl:call-template>
				</div>
			</fieldset>
		</div>
   </xsl:template>
   <!-- The cluster layout-->      
   <xsl:template name="cluster">
		<xsl:param name="clusterTitle"/>
		<div class="cluster" name="{generate-id($clusterTitle)}" style="float:left; width: 100%;" align="left">
			<fieldset class="fieldset">			
				<legend class="fieldset-legend"><xsl:value-of select="$clusterTitle"/></legend>
				<div style="width: 100%;" align="left">
					<xsl:call-template name="elementIterator">
						<xsl:with-param name="iterator" select="parts"/>						
					</xsl:call-template>
				</div>
			</fieldset>
		</div>
   </xsl:template>
	<xsl:template name="chooseDataTypeFormat">
		<xsl:choose>
			<xsl:when test="contains(value/@xsi:type,'INT')">
				<xsl:call-template name="dataType">
					<xsl:with-param name="value" select="value/value"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="contains(value/@xsi:type,'CV')">
				<xsl:call-template name="dataType">
					<xsl:with-param name="value" select="value/codeValue"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="contains(value/@xsi:type,'SIMPLE_TEXT')">
				<xsl:call-template name="dataType">
					<xsl:with-param name="value" select="value/originalText"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="contains(value/@xsi:type,'TS')">
				<xsl:call-template name="dataType">
					<xsl:with-param name="value" select="value/time"/>
				</xsl:call-template>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="dataType">
		<xsl:param name="value"/>
		<div style="float:left; margin-right: 2px; padding-bottom: 2px;" class="dataType">
				<label for="{generate-id(name/originalText)}"><xsl:value-of select="name/originalText"/>:</label>
				<div style="height: 35px;" name="{generate-id(name/originalText)}">
					<input type="text" value="{$value}"/>
				</div>
			</div> 
	</xsl:template>  

   <xsl:template name="addCSS">
      <style type="text/css">
         <xsl:text>
body {
  color: #003366;
  background-color: #FFFFFF;
  font-family: Verdana, Tahoma, sans-serif;
  font-size: 11px;
}

a {
  color: #003366;
  background-color: #FFFFFF;
}

h1 {
  font-size: 12pt;
  font-weight: bold;
}

h2 {
  font-size: 11pt;
  font-weight: bold;
}

h3 {
  font-size: 10pt;
  font-weight: bold;
}

h4 {
  font-size: 8pt;
  font-weight: bold;
}



table {
  line-height: 10pt;
  width: 80%;
}

tr {
  background-color: #ccccff;
}

td {
  padding: 0.1cm 0.2cm;
  vertical-align: top;
}

.h1center {
  font-size: 12pt;
  font-weight: bold;
  text-align: center;
  width: 80%;
}

.header_table{
  border: 1pt inset #00008b;
}

.narr_table {
  width: 100%;
}

.narr_tr {
  background-color: #ffffcc;
}

.narr_th {
  background-color: #ffd700;
}

.td_label{
  font-weight: bold;
  color: white;
}
.section { float: left; border: 0px solid #cdcdcd; margin: 29px 0; }
.middleNav { float: left; border: 0px solid #cdcdcd; margin: 29px 0; }
.middleNav ul { }
.middleNav ul li { display: block; float: left; border-left: 1px solid #cdcdcd; position: relative; }
.middleNav ul li:first-child { border-left: none; }
.middleNav ul li a { width: 60px; height: 45px; display: block; border: 1px solid #fbfbfb; cursor: pointer; background: #e9e9e9 url(../images/backgrounds/middleNavBg.png) repeat-x; }
.middleNav ul li a:hover { background: #f3f3f3; }
.middleNav ul li a:active { background: #efefef; }
.middleNav ul li a span { display: block; height: 45px; }


.middleNav ul.tabs { background: url(../images/backgrounds/titleBg.png) repeat-x; height: 36px; border-bottom: 1px solid #CDCDCD; }
.middleNav ul.tabs li { float: left; height: 36px; line-height: 38px; border-left: none; overflow: hidden; position: relative; font-size: 15px; border-right: 1px solid #cdcdcd; }
.middleNav ul.tabs li a { display: block; padding: 0px 12px; outline: none; color: #424242; font-size: 12px; font-weight: bold; }
.middleNav ul.tabs li a:hover { color: #797979; }

html ul.tabs li.activeTab  { background-color: #f9f9f9; height: 37px; }
.rightTabs .activeTab { height: 36px!important; }
html ul.tabs li.activeTab a { color: #2E6B9B; } 

.tab_container { overflow: hidden; width: 100%; }
.tab_content { padding: 10px 12px; }

.rightTabs { position: relative; }
.rightTabs ul.tabs { float: right; background: none; height: 38px; position: absolute; top: 0; right: 0; border-bottom: none; }
.rightTabs ul.tabs li { border-left: 1px solid #cdcdcd; border-right: none; }

fieldset.fieldset {
	background: #F6F6F6;
	border-color: #686868 !important;
	-moz-border-radius: 6px;
	-webkit-border-radius: 6px;
}
legend.fieldset-legend {
	background: #252525;
	color: #DBDBDB;
	-moz-border-radius: 4px;
	-webkit-border-radius: 4px;
}
.generalInput { width: 190px; float: right; margin-right: 14px!important; }
.formRow { padding: 18px 14px; }
input{
	width: 190px;
	overflow: auto;
	
	
}
.dataType{
	background: #252525;
	color: #DBDBDB;
	-moz-border-radius: 4px;
	-webkit-border-radius: 4px;
}
          </xsl:text>
      </style>
   </xsl:template>
</xsl:stylesheet>
