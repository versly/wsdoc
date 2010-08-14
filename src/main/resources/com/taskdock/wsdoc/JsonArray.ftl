<#--
~ Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
-->

<#macro render_json_array json>
<#-- @ftlvariable name="json" type="com.taskdock.wsdoc.JsonDict" -->

<span class="json-array">[
    <@render_json json.elementType />
]</span>
</#macro>