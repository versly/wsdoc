<!--
~ Copyright (c) Versly, Inc. 2009-2010. All Rights Reserved.
-->

<#-- @ftlvariable name="docs" type="java.util.List<com.versly.rest.wsdoc.RestDocumentation>" -->

<html>
    <head>
        <title>REST Endpoint Documentation</title>
        <style type="text/css">
            div.section-title { font-size: 18px; font-weight: bold; }
            div.resource-summary { font-family: monospace; padding-top: 10px; }
            .resource-summary div { padding-left: 15px }

            div.resource { border-top: 1px solid gray; padding-top: 5px; margin-top: 15px; }
            div.resource-header { font-family: monospace; font-size: 18px; font-weight: bold; padding-bottom: 15px; }
            div.resource-docs { padding-bottom: 20px; }

            div.url-info { padding-bottom: 20px; }
            div.url-info table { width: 400px; border-spacing: 0px; }
            div.url-info thead td { border-bottom: 1px dashed gray; }
            .url-info-key { font-family: monospace; }
            .url-info-expected-type { font-family: monospace; }

            div.request-body { padding-bottom: 20px; }
            div.response-body { padding-bottom: 20px; }

            div.body-title { width: 400px; border-bottom: 1px dashed gray; }

            div.body-contents { font-family: monospace; }
            div.json-field { padding-left: 15px; }
            span.json-field-name { font-weight: bold; }
            span.json-primitive-type { color: gray; }
            div.json-primitive-restrictions { color: gray; padding-left: 30px; }
            div.json-field-comment { color: gray; padding-left: 30px; }
        </style>
    </head>
    <body>

        <div class="section-title">Overview</div>
        <#list docs as doc>
            <#list doc.resources as resource>
                <div class="resource-summary">
                    <span class="resource-summary-path">${resource.path}</span>
                    <div>
                        <#list resource.requestMethodDocs?sort_by("requestMethod") as methodDoc>
                            <a href="#${methodDoc.key}">${methodDoc.requestMethod}</a>
                        </#list>
                    </div>
                </div>
            </#list>
        </#list>

        <#list docs as doc>
            <#list doc.resources as resource>
                <#list resource.requestMethodDocs as methodDoc>
                    <a id="${methodDoc.key}"/>
                    <div class="resource">
                        <div class="resource-header">
                                <span class="method">${methodDoc.requestMethod}</span>
                                <span class="path">${resource.path}</span><#t>
                                <#-- append any URL parameters to the end of the main banner -->
                                <#assign params=methodDoc.urlParameters.fields><#t>
                                <#if (params?keys?size > 0)><#t>
                                    ?<#list params?keys as key><#t>
                                        <span class="url-param-key">${key}</span><#t>
                                        =<#t>
                                        <span class="url-param-expected-type"><@render_json params[key]/></span><#t>
                                        <#if key_has_next>&</#if><#t>
                                    </#list><#t>
                                </#if><#t>
                        </div>

                        <#if (methodDoc.commentText??)>
                            <div class="resource-docs">
                                ${methodDoc.commentText}
                            </div>
                        </#if>

                        <#if methodDoc.multipartRequest>
                            <div class="multipart-notice">
                                Note: this endpoint expects a multipart request body.
                            </div>
                        </#if>

                        <#assign subs=methodDoc.urlSubstitutions.fields>
                        <#if (subs?keys?size > 0)>
                            <div class="url-info">
                                <table>
                                    <thead>
                                        <tr><td>URL Substitution Key</td><td>Expected Type</td></tr>
                                    </thead>
                                    <#list subs?keys as key>
                                        <tr>
                                            <td class="url-info-key">${key}</td>
                                            <td class="url-info-expected-type"><@render_json subs[key]/></td>
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
        </#list>
    </body>
</html>

<#macro render_json json>
    <#if json.class.name == "com.versly.rest.wsdoc.JsonPrimitive">
        <@render_json_primitive json/>
    <#elseif json.class.name == "com.versly.rest.wsdoc.JsonObject">
        <@render_json_object json/>
    <#elseif json.class.name == "com.versly.rest.wsdoc.JsonArray">
        <@render_json_array json/>
    <#elseif json.class.name == "com.versly.rest.wsdoc.JsonDict">
        <@render_json_dict json/>
    </#if>
</#macro>

<#macro render_json_array json>
<#-- @ftlvariable name="json" type="com.versly.rest.wsdoc.JsonArray" -->
    <span class="json-array">[
        <@render_json json.elementType />
    ]</span>
</#macro>

<#macro render_json_dict json>
<#-- @ftlvariable name="json" type="com.versly.rest.wsdoc.JsonDict" -->
    <span class="json-dict">[
        <@render_json json.keyType/>
        -&gt;
        <@render_json json.valueType/>
    ]</span>
</#macro>

<#macro render_json_primitive json>
<#-- @ftlvariable name="json" type="com.versly.rest.wsdoc.JsonPrimitive" -->
    <span class="json-primitive-type">${json.typeName}</span><#t>
    <#if json.restrictions??><#t>
        <div class="json-primitive-restrictions"><#t>
            one of [ <#list json.restrictions as restricton><#t>
                ${restricton}<#if restricton_has_next>, </#if><#t>
            </#list> ]</div><#t>
    </#if><#t>
</#macro>

<#macro render_json_object json>
<#-- @ftlvariable name="json" type="com.versly.rest.wsdoc.JsonObject" -->
    <span class="json-object">{
    <div class="json-fields">
        <#list json.fields as field>
            <div class="json-field">
                <span class="json-field-name">${field.fieldName}</span>
                <@render_json field.fieldType/>
                <#if field.commentText??>
                    <div class="json-field-comment">${field.commentText}</div>
                </#if>
            </div>
        </#list>
    </div><#t>
    </span><#t>
    }
</#macro>
