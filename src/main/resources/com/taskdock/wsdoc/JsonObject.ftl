<#--
~ Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
-->

<#macro render_json_object json indent>
<#-- @ftlvariable name="json" type="com.taskdock.wsdoc.JsonObject" -->
<#-- @ftlvariable name="indent" type="int" -->

<span class="json-object">{
<div class="json-fields">
    <#list json.fields as field>
        <div class="json-field">
            <span class="json-field-name">${field.fieldName}</span>
            <@render_json field.fieldType indent + 1/>
        </div>
    </#list>
</div>
</span>
}
</#macro>