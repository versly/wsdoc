<#-- @ftlvariable name="api" type="org.versly.rest.wsdoc.impl.RestDocumentation.RestApi" -->
#%RAML 0.8
---
<#if api.apiTitle??>
title: ${api.apiTitle}
<#else>
title:
</#if>
<#if api.apiVersion??>
version: ${api.apiVersion}
</#if>
<#if api.mount??>
baseUri: ${api.mount}
</#if>
<#if api.apiDocumentation??>
documentation:
    - title: Overview
      content: |
${api.indentedApiDocumentationText(10)}
</#if>

<#if api.getTraits()?size gt 0>
traits:
${api.indentedApiTraits(4)}
</#if>

<#list api.resources as resource>
<#if !resource.parent??>
<@write_resource resource=resource depth=0/>
</#if>
</#list>


<#--
  -- write out a RAML resource path.
  -- Note, we strip off regex expressions because RAML requires the path to be a valid URI template.
  -->
<#macro write_resource resource depth>
<#if (depth > 0)><#list 1..depth as i> </#list></#if>${resource.pathLeaf?replace(":.*}", "}", "r")}:
<@write_uri_parameters resource=resource depth=depth+4 />
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
  
<@write_traits methodDoc=methodDoc depth=depth+4/>

<@write_parameters methodDoc=methodDoc depth=depth+4/>

<#if methodDoc.requestSchema??>
<@write_body schema=methodDoc.requestSchema example=methodDoc.requestExample depth=depth+4/>

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
  -- write out method traits
  -->
<#macro write_traits methodDoc depth>
<#list 1..depth as i> </#list>is: ${methodDoc.traitsAsString}
</#macro>

<#--
  -- write out all URI parameters for a method
  -->
<#macro write_uri_parameters resource depth>
<#list 1..depth as i> </#list>uriParameters:
<#assign fields=resource.resourceUrlSubstitutions.fields>
<#list fields?keys as key>
<@write_parameter fields=fields key=key depth=depth+4/>
</#list>
</#macro>

<#--
  -- write out all query parameters for a method
  -->
<#macro write_parameters methodDoc depth>
<#list 1..depth as i> </#list>queryParameters:
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
<#list 1..depth as i> </#list>description: |
<#list 1..depth as i> </#list>    ${field.fieldDescription!}
<#if field.fieldType.class.name == "org.versly.rest.wsdoc.impl.JsonPrimitive">
<#list 1..depth as i> </#list>type: <@write_raml_type field.fieldType.typeName/>
<#if field.fieldType.restrictions??><#t>
<#list 1..depth as i> </#list>enum: [ <#list field.fieldType.restrictions as restricton>${restricton}<#if restricton_has_next>, </#if></#list> ]</#if>
<#else>
<#list 1..depth as i> </#list>type: string
</#if>
</#macro>


<#--
  -- write out request or response body
  -->
<#macro write_body schema example depth>
<#list 1..depth as i> </#list>body:
<@write_body_media_type schema=schema example=example depth=depth+4/>
</#macro>


<#--
  -- write out media type of request body
  -- (TODO: allow for more interesting media types)
  -->
<#macro write_body_media_type schema example depth>
<#list 1..depth as i> </#list>application/json:
<@write_body_schema schema=schema depth=depth+4/>
<@write_body_example example=example depth=depth+4/>
</#macro>


<#--
  -- write out all url parameters for a method
  -->
<#macro write_body_schema schema depth>
<#list 1..depth as i> </#list>schema: |
<#list 1..depth as i> </#list>    ${schema?trim}
</#macro>

<#macro write_body_example example depth>
<#list 1..depth as i> </#list>example: |
<#list 1..depth as i> </#list>    ${example?trim}
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
<@write_body schema=methodDoc.responseSchema example=methodDoc.responseExample depth=depth+4/>
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