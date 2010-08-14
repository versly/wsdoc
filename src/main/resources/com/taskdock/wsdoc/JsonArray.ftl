<#--
~ Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
-->

<#macro render_json_array json>
<#-- @ftlvariable name="json" type="com.taskdock.wsdoc.JsonDict" -->
<#-- @ftlvariable name="indent" type="int" -->

<span style="width: 40%">[</span>
<div class="json-dict">
    <@render_json json.elementType/>
</div>
<span style="width: 40%">]</span>
</#macro>