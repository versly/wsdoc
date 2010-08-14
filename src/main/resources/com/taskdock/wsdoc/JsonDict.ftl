<#--
~ Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
-->

<#macro render_json_dict json indent>
<#-- @ftlvariable name="json" type="com.taskdock.wsdoc.JsonDict" -->
<#-- @ftlvariable name="indent" type="int" -->

<span class="json-dict">[
<div>
    <@render_json json.keyType indent + 1/>
    -&gt;
    <@render_json json.valueType indent + 1/>
</div>
]</span>
</#macro>