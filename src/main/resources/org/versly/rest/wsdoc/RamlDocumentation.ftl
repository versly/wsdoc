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
  -- write out a RAML resource path.
  -- Note, we strip off regex expressions because RAML requires the path to be a valid URI template.
  -->
<#macro write_resource resource depth>
<#if (depth > 0)><#list 1..depth as i> </#list></#if>${resource.pathLeaf?replace(":.*}", "}", "r")}:
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
<@write_description methodDoc=methodDoc depth=depth+4/>
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
<#macro write_description methodDoc depth>
<#list 1..depth as i> </#list>description: |
${methodDoc.indentedCommentText(depth+4)}
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
<@write_parameter_info field=fields[key] depth=depth+4/>
</#macro>


<#--
  -- write out a single url parameter type
  -->
<#macro write_parameter_info field depth>
<#if field.fieldDescription??>
<#list 1..depth as i> </#list>description: |
<#list 1..depth as i> </#list>    ${field.fieldDescription}
</#if>
<#if field.fieldType.class.name == "org.versly.rest.wsdoc.impl.JsonPrimitive">
<#list 1..depth as i> </#list>type: <@write_raml_type field.fieldType.typeName/>
<#if field.fieldType.restrictions??><#t>
one of [ <#list field.fieldType.restrictions as restricton>${restricton}<#if restricton_has_next>, </#if></#list> ]</#if>
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


<#--
  -- RAML has a limited set of types for URI template parameters
  -->
<#macro write_raml_type type>
<#if type == "float" || type == "double">number
<#elseif type == "integer" || type == "long" || type == "short" || type == "byte" >integer
<#elseif type == "date" || type == "timestamp" || type == "time">date
<#elseif type == "boolean">boolean
<#else>string
</#if>
</#macro>