<!--
~ Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
-->

<#-- @ftlvariable name="docs" type="com.taskdock.wsdoc.RestDocumentation" -->

<#include "JsonPrimitive.ftl">
<#include "JsonObject.ftl">
<#include "JsonDict.ftl">
<#include "JsonArray.ftl">

<#macro render_json json>
    <#if json.class.name == "com.taskdock.wsdoc.JsonPrimitive">
        <@render_json_primitive json/>
    <#elseif json.class.name == "com.taskdock.wsdoc.JsonObject">
        <@render_json_object json/>
    <#elseif json.class.name == "com.taskdock.wsdoc.JsonArray">
        <@render_json_array json/>
    <#elseif json.class.name == "com.taskdock.wsdoc.JsonDict">
        <@render_json_dict json/>
    </#if>
</#macro>

<html>
    <head>
        <title>REST Endpoint Documentation</title>
    </head>
    <body>
        <#list docs.resources as resource>
            <#list resource.requestMethodDocs as methodDoc>
                <div class="resource">
                    <span class="method">${methodDoc.requestMethod}</span>
                    <span class="path">${resource.path}</span>

                    <#assign subs=methodDoc.urlSubstitutions.substitutions>
                    <#if (subs?keys?size > 0)>
                        <div class="url-subs">
                            <div class="url-subs-title">URL Substitutions</div>
                            <table>
                                <thead>
                                    <tr><td>Substitution Key</td><td>Expected Type</td></tr>
                                </thead>
                                <#list subs?keys as key>
                                    <tr>
                                        <td class="url-sub-key">${key}</td>
                                        <td class="url-sub-expected-type"><@render_json subs[key]/></td>
                                    </tr>
                                </#list>
                            </table>
                        </div>
                    </#if>

                    <#if (methodDoc.requestBody??)>
                        <div class="request-body">
                            <div class="body-title">Request Body</div>
                            <div class="body-contents"><@render_json methodDoc.requestBody/></div>
                        </div>
                    </#if>

                    <#if (methodDoc.responseBody??)>
                        <div class="response-body">
                            <div class="body-title">Response Body</div>
                            <div class="body-contents"><@render_json methodDoc.responseBody/></div>
                        </div>
                    </#if>
                </div>
            </#list>
        </#list>
    </body>
</html>