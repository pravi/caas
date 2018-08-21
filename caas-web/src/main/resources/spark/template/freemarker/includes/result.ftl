<#ftl output_format="HTML">
<#macro result>
<h3>Results</h3>
<div class="results_container">
    <#if results?size != 0>
    <#list results as result>
        <#if !result.getTest().informational()>
            <#if result.success>
        <div class="result chip clickable passed" onclick="location.href='/test/${result.getTest().short_name()}'">
            ${result.getTest().full_name()}
            <div class="result_image">
                <img src="/img/passed.svg">
            </div>
        </div>
            <#else>
        <div class="result chip clickable failed" onclick="showHelp('${result.getTest().short_name()}')">
            ${result.getTest().full_name()}
            <div class="result_image">
                <img src="/img/passed.svg">
            </div>
        </div>
            </#if>
        </#if>
    </#list>
</div>
<h3>Results for informational tests</h3>
<div class="results_container">
        <#list results as result>
            <#if result.getTest().informational()>
                <#if result.success>
        <div class="result chip clickable passed" onclick="location.href='/test/${result.getTest().short_name()}'">
            ${result.getTest().full_name()}
            <div class="result_image">
                <img src="/img/passed.svg">
            </div>
        </div>
                <#else>
        <div class="result chip clickable failed" onclick="showHelp('${result.getTest().short_name()}')">
            ${result.getTest().full_name()}
            <div class="result_image">
                <img src="/img/failed.svg">
            </div>
        </div>
                </#if>
            </#if>
        </#list>
</div>
    <#else>
    <h2 class="error_message">
        Results unavailable for ${domain}
    </h2>
    </#if>
</#macro>
