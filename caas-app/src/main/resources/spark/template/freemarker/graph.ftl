<#ftl output_format="HTML">
<#assign height=500>
<#macro graph>

    <noscript>Enable Javascript to see graphs</noscript>
    <div class="card" id="chart_container" height="${height + 20}px">
        <svg id="svg_container" shape-rendering="optimizeQuality" preserveAspectRatio="xMinYMin">
        </svg>
    </div>

    <div class="card" id="tooltip"></div>
</#macro>
