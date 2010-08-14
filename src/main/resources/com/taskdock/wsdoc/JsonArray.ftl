<#--
~ Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
-->

<#macro render_json_array json indent>
<#-- @ftlvariable name="json" type="com.taskdock.wsdoc.JsonDict" -->
<#-- @ftlvariable name="indent" type="int" -->

<span class="json-array">[</span>
<div class="json-dict">
    <@render_json json.elementType indent + 1/>
</div>
<span style="width: 40%">]</span>
</#macro>