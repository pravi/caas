<#ftl output_format="HTML">
<#macro table>
<div id="cover_table_hack"></div>
<div id="div_header"></div>
<div id="div_first_col">
<#list resultsByServer as domain,results>
    <div class="server_name">
        <a href="/server/${domain}">
            ${domain}
        </a>
    </div>
</#list>
</div>
<div id="results_table" class="fixed-table-container">
    <#if resultsByServer?has_content>
        <table>
            <thead>
            <tr>
                <th>Server</th>
                <th>Compliance</th>
        <#list tests as test>
                <th>
                    <a href="/test/${test.short_name()}">
                        ${test.full_name()}
                    </a>
                </th>
        </#list>
            </tr>
            </thead>
            <tbody>
        <#list resultsByServer as domain,results>
        <tr server="${domain}">
            <td>
                <a href="/server/${domain}">
                    ${domain}
                </a>
            </td>
            <td class="text">
                ${percentByServer[domain]}
            </td>
            <#list tests as test>
                <#if results[test.short_name()]??>
            <td class="${results[test.short_name()]?then("passed" ,"failed")}">
            </td>
                <#else>
            <td class="not_found">
            </td>
                </#if>
            </#list>
        </tr>
        </#list>
            </tbody>
        </table>
    <#else>
    <h2>
        ¯\_(⊙︿⊙)_/¯
    </h2>
    <h3>
        ${(no_results_found_msg??)?then(no_results_found_msg!,"No results found")}
    </h3>
    </#if>
</div>
</#macro>
