<#--
~ Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
-->

<#macro render_json_dict json>
<#-- @ftlvariable name="json" type="com.taskdock.wsdoc.JsonDict" -->

<span class="json-dict">[
    <@render_json json.keyType/>
    -&gt;
    <@render_json json.valueType/>
]</span>
</#macro>