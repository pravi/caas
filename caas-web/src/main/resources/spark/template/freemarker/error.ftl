<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#assign title="Error ${error_code} | ${page.project_name}" in page>
<@page.page>
<div class="card error">
    <h1>
        Error ${error_code}
    </h1>
    <h2>
        ¯\_(⊙︿⊙)_/¯
    </h2>
    <h3>${error_msg}</h3>
</div>
</@page.page>