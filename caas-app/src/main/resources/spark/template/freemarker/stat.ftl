<#ftl output_format="HTML">
<#macro stat>
    <style>

        #server_stats {
            display: flex;
            justify-content: space-around;
        }

        .stat {
            display: inline-flex;
            flex-direction: column;
        }

        .stat_result {
            color: ${page.green};
            font-size: 3em;
        }

    </style>
        <div id="stats">
        <#list stats as stat,value>
            <div class="server_stat">
                <div class="stat_result">
                    ${value}
                </div>
                <div class="stat_info">
                    ${stat}
                </div>
            </div>
        </#list>
        </div>
</#macro>
