<#--
~ Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
-->

<#macro render_json_object json>
<#-- @ftlvariable name="json" type="com.taskdock.wsdoc.JsonObject" -->
<#-- @ftlvariable name="indent" type="int" -->

<span style="width: 40%">{</span>
<div class="json-fields">
    <#list json.fields as field>
    <span class="json-field" style="width: 40%">${field.fieldName}</span>
    <@render_json field.fieldType/>
    </#list>
</div>
<span style="width: 40%">}</span>
</#macro>