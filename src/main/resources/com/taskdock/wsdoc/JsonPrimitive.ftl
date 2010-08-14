<#--
~ Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
-->

<#macro render_json_primitive json>
<#-- @ftlvariable name="json" type="com.taskdock.wsdoc.JsonPrimitive" -->

<span class="json-primitive-type" style="width: 20%">${json.typeName}</span>
<#if json.restrictions??>
    <span class="json-primitive-restrictions" style="width: 20%">
        one of [ <#list json.restrictions as restricton>
            ${restricton}<#if restricton_has_next>, </#if>
        ]</#list></span>
</#if>
</#macro>