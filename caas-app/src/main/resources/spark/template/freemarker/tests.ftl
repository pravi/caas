<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#assign description="Check compliance status for XMPP extensions and other compliance tests across servers" in page>
<#assign title="${page.project_name}: Check " in page>
<#assign stylesheets=["/css/tests.css"] in page>
<@page.page>
    <h2>Compliance tests</h2>
    <div id="test_container">
    <#list tests as test>
        <div class="test card">
            <h2 style="display: inline;">${test.full_name()}</h2>
            <p>${test.description()}</p>
            <a href="/test/${test.short_name()}">
                <button class="floating_button">
                    +
                </button>
            </a>
        </div>
    </#list>
    </div>
</@page.page>