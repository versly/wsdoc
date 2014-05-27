<#-- @ftlvariable name="docs" type="java.util.List<org.versly.rest.wsdoc.impl.RestDocumentation>" -->
#%RAML 0.8
---
title: REST
protocols: [ HTTPS ]
mediaType: application/json


<#list docs as doc>
<#list doc.resources as resource>
<#if !resource.parent??>
<@write_resource resource=resource depth=0/>
</#if>
</#list>
</#list>


<#--
  -- write out a RAML resource
  -->
<#macro write_resource resource depth>
<#if (depth > 0)><#list 1..depth as i> </#list></#if>${resource.pathLeaf}:
<@write_resource_parts resource=resource depth=depth+4/>

</#macro>


<#--
  -- write out methods of the RAML resource and then the sub-resources
  -->
<#macro write_resource_parts resource depth>
<#list resource.requestMethodDocs as methodDoc>
<@write_method methodDoc=methodDoc depth=depth/>

</#list>
<#list resource.children as child>
<@write_resource resource=child depth=depth/>

</#list>
</#macro>


<#--
  -- write out the method name, description, parameters, body, and response for the given method
  -->
<#macro write_method methodDoc depth>
<#list 1..depth as i> </#list>${methodDoc.requestMethod?lower_case}:
<#if methodDoc.commentText??>
<@write_description description=methodDoc.commentText depth=depth+4/>
</#if>

<@write_parameters methodDoc=methodDoc depth=depth+4/>

<#if methodDoc.requestSchema??>
<@write_body body=methodDoc.requestSchema depth=depth+4/>

</#if>
<@write_response methodDoc=methodDoc depth=depth+4/>
</#macro>


<#--
  -- write out method description
  -->
<#macro write_description description depth>
<#list 1..depth as i> </#list>description: ${description?trim}
</#macro>


<#--
  -- write out all url parameters for a method
  -->
<#macro write_parameters methodDoc depth>
<#list 1..depth as i> </#list>queryParameters:
<#assign fields=methodDoc.urlSubstitutions.fields>
<#list fields?keys as key>
<@write_parameter fields=fields key=key depth=depth+4/>
</#list>
<#assign fields=methodDoc.urlParameters.fields>
<#list fields?keys as key>
<@write_parameter fields=fields key=key depth=depth+4/>
</#list>
</#macro>


<#--
  -- write out a single query parameter name and type
  -->
<#macro write_parameter fields key depth>
<#list 1..depth as i> </#list>${key}:
<@write_parameter_type type=fields[key] depth=depth+4/>
</#macro>


<#--
  -- write out a single url parameter type
  -->
<#macro write_parameter_type type depth>
<#if type.class.name == "org.versly.rest.wsdoc.impl.JsonPrimitive">
<#list 1..depth as i> </#list>type: ${type.typeName}<#t>
<#if type.restrictions??><#t>
one of [ <#list type.restrictions as restricton>${restricton}<#if restricton_has_next>, </#if></#list> ]</#if>
<#else>
<#list 1..depth as i> </#list>type: string
</#if>
</#macro>


<#--
  -- write out request or response body
  -->
<#macro write_body body depth>
<#list 1..depth as i> </#list>body:
<@write_body_media_type body=body depth=depth+4/>
</#macro>


<#--
  -- write out media type of request body
  -- (TODO: allow for more interesting media types)
  -->
<#macro write_body_media_type body depth>
<#list 1..depth as i> </#list>application/json:
<@write_body_schema body=body depth=depth+4/>
</#macro>


<#--
  -- write out all url parameters for a method
  -->
<#macro write_body_schema body depth>
<#list 1..depth as i> </#list>schema: '${body?trim}'
</#macro>


<#--
  -- write out response for a method
  -->
<#macro write_response methodDoc depth>
<#list 1..depth as i> </#list>responses:
<@write_response_code methodDoc=methodDoc depth=depth+4/>
</#macro>


<#--
  -- write out response code for a method
  -->
<#macro write_response_code methodDoc depth>
<#list 1..depth as i> </#list>200:
<#if methodDoc.responseSchema??>
<@write_body body=methodDoc.responseSchema depth=depth+4/>
</#if>
</#macro>



