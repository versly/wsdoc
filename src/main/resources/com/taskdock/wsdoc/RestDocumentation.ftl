<!--
~ Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
-->

<#-- @ftlvariable name="docs" type="com.taskdock.wsdoc.RestDocumentation" -->

<#include "JsonPrimitive.ftl">
<#include "JsonObject.ftl">
<#include "JsonDict.ftl">
<#include "JsonArray.ftl">

<#macro render_json json indent>
    <#if json.class.name == "com.taskdock.wsdoc.JsonPrimitive">
        <@render_json_primitive json/>
    <#elseif json.class.name == "com.taskdock.wsdoc.JsonObject">
        <@render_json_object json indent/>
    <#elseif json.class.name == "com.taskdock.wsdoc.JsonArray">
        <@render_json_array json indent/>
    <#elseif json.class.name == "com.taskdock.wsdoc.JsonDict">
        <@render_json_dict json indent/>
    </#if>
</#macro>

<html>
    <head>
        <title>REST Endpoint Documentation</title>
        <style type="text/css">
            div.resource { border-top: 1px solid gray; padding-top: 5px; margin-top: 15px; }
            div.resource-header { font-family: monospace; font-size: 18px; font-weight: bold; padding-bottom: 15px; }

            div.url-subs { padding-bottom: 20px; }
            div.url-subs table { width: 400px; border-spacing: 0px; }
            div.url-subs thead td { border-bottom: 1px dashed gray; }
            .url-sub-key { font-family: monospace; }
            .url-sub-expected-type { font-family: monospace; }

            div.request-body { padding-bottom: 20px; }
            div.response-body { padding-bottom: 20px; }

            div.body-contents { font-family: monospace; }
            div.json-field { padding-left: 15px; }
            span.json-field-name { font-weight: bold; }
            div.json-field span.json-primitive-type { color: gray; }
            div.json-primitive-restrictions { color: gray; padding-left: 30px; }
        </style>
    </head>
    <body>
        <#list docs.resources as resource>
            <#list resource.requestMethodDocs as methodDoc>
                <div class="resource">
                    <div class="resource-header">
                        <span class="method">${methodDoc.requestMethod}</span>
                        <span class="path">${resource.path}</span>
                    </div>

                    <#assign subs=methodDoc.urlSubstitutions.substitutions>
                    <#if (subs?keys?size > 0)>
                        <div class="url-subs">
                            <table>
                                <thead>
                                    <tr><td>URL Substitution Key</td><td>Expected Type</td></tr>
                                </thead>
                                <#list subs?keys as key>
                                    <tr>
                                        <td class="url-sub-key">${key}</td>
                                        <td class="url-sub-expected-type"><@render_json subs[key] 0/></td>
                                    </tr>
                                </#list>
                            </table>
                        </div>
                    </#if>

                    <#if (methodDoc.requestBody??)>
                        <div class="request-body">
                            <div class="body-title">Request Body</div>
                            <div class="body-contents"><@render_json methodDoc.requestBody 0/></div>
                        </div>
                    </#if>

                    <#if (methodDoc.responseBody??)>
                        <div class="response-body">
                            <div class="body-title">Response Body</div>
                            <div class="body-contents"><@render_json methodDoc.responseBody 0/></div>
                        </div>
                    </#if>
                </div>
            </#list>
        </#list>
    </body>
</html>