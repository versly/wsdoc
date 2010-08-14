<#--
~ Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
-->

<#macro render_json_object json>
<#-- @ftlvariable name="json" type="com.taskdock.wsdoc.JsonObject" -->

<span class="json-object">{
<div class="json-fields">
    <#list json.fields as field>
        <div class="json-field">
            <span class="json-field-name">${field.fieldName}</span>
            <@render_json field.fieldType/>
        </div>
    </#list>
</div>
</span>
}
</#macro>