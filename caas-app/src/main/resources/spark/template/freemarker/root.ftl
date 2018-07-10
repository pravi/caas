<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#assign description="Check compliance status for XMPP servers" in page>
<#assign title="${page.project_name}: Check compliance status for XMPP servers" in page>
<@page.page>
    <h2>Compliance status</h2>
<table>
    <tr>
        <th>Server</th>
    <#list tests as test>
    <th>
        ${test.full_name()}
    </th>
    </#list>
    </tr>
    <div class="passed"></div>

        <#if resultsByServer??>
            <#list resultsByServer as domain,results>
            <tr>
                <td>
                    <a href="/server/${domain}">
                        ${domain}
                    </a>
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
        <#else>
        <h3>
            No results found. Add credentials for some XMPP servers to get started
        </h3>
        </#if>
</table>

</@page.page>