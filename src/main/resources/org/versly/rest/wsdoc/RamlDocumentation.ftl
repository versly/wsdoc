<#-- @ftlvariable name="docs" type="java.util.List<org.versly.rest.wsdoc.impl.RestDocumentation>" -->
#%RAML 0.8
---
title: REST API Documentation
baseUri: https://example.com/foo/api/{version}
version: v1

<#list docs as doc>
<#list doc.resources as resource>
<#if !resource.parent??>
<@write_resource resource=resource depth=0/>
</#if>
</#list>
</#list>

<#macro write_resource resource depth>
<#if (depth > 0)><#list 1..depth as i> </#list></#if>${resource.pathLeaf}:
<@write_resource_parts resource=resource depth=depth+4/>

</#macro>

<#macro write_resource_parts resource depth>
<#list resource.requestMethodDocs as methodDoc>
<#list 1..depth as i> </#list>${methodDoc.requestMethod?lower_case}:

</#list>
<#list resource.children as child>
<@write_resource resource=child depth=depth/>

</#list>
</#macro>
