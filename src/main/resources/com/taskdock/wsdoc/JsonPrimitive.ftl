<#--
~ Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
-->

<#macro render_json_primitive json>
<#-- @ftlvariable name="json" type="com.taskdock.wsdoc.JsonPrimitive" -->

<span class="json-primitive-type">${json.typeName}</span>
<#if json.restrictions??>
    <div class="json-primitive-restrictions">
        one of [ <#list json.restrictions as restricton>
            ${restricton}<#if restricton_has_next>, </#if>
        </#list> ]</div>
</#if>
</#macro>