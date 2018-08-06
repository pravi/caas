<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#assign scripts=["/js/table.js"] in page>
<#assign stylesheets=["/css/table.css"] in page>
<#assign title="Check compliance status for XMPP servers | ${page.project_name}" in page>
<@page.page>
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
            <tr>
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
            No results found. <a href="/add">Add</a> credentials for some XMPP servers to get started
        </h3>
        </#if>
    </div>
    <#assign footerText=" Problems viewing the table? <a href='#' id='reset_table'> Turn off sticky headers </a> " in page>
</@page.page>