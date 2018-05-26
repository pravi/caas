<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#assign description="Server compliance result for ${domain}" in page>
<#assign title="Compliance result for ${domain}" in page>
<@page.page>
    <style>
        #server_stats {
            display: flex;
            justify-content: space-around;
            justify-content: space-around;
        }

        .server_stat {
            display: inline-flex;
            flex-direction: column;
        }

        .stat_result {
            color: #43A047;
            font-size: 3em;
        }

        #server_results {
            padding-top: 20px;
            padding-bottom: 20px;
        }

        .server_failed {
            cursor: pointer;
        }

        .server_failed::after {
            content: url('/img/failed.svg');
        }

        .server_passed::after {
            content: url('/img/passed.svg');
        }

        #server_run {
            padding: 30px;
        }

        #additional_server {
            margin: 50px auto;
            display: inline-flex;
            justify-content: space-between;
        }

        #additional_server > div {
            margin: 20px 10px;
            padding: 15px;
        }

        .input_subscribe {
            width: 250px;
        }

        #help_server {
            display: inline-flex;
            flex-wrap: wrap;
            justify-content: space-around;
        }

        .help {
            margin: 15px 10px;
            padding: 20px;
            text-align: left;
        }

        @media only screen and (max-width: 850px) {
            #additional_server {
                display: block;
            }
        }
    </style>
     <h2> Compliance status for ${domain}</h2>
    <div id="server_stats">
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

    <button id="download_report" href="#">Download report</button>
    <br><br>
    Server is running ${softwareName} ${softwareVersion}
    <br><br>
    <div id="server_results">
        <#list results as result>
            <#if result.success>
                <div class="server_result chip server_passed">
            <#else>
                <div class="server_result chip server_failed  onclick="
                     window.location='#${result.getTest().short_name()}'
                ">
            </#if>
            ${result.getTest().full_name()}
        </div>
        </#list>
    </div>

    <div id="server_run">
        Tests last ran ${timeSince}<br>
        <button onclick="location.href='/live/${domain}'">Rerun tests</button>
    </div>

     <div id="additional_server">
         <div class="card" id="subscribe_server">
             <h3>Subscribe to periodic reports for this server</h3>
             <form id="form_add" action="#subscribe" method="post">
                 <div>
                     <label for="email" class="input_label_subscribe">E-Mail</label>
                     <input name="jid" class="input_subscribe" type="text"/>
                 </div>
                 <div>
                     <input type="submit" class="button" id="subscribe_button" value="Subscribe"/>
                 </div>
             </form>
         </div>
         <div class="card" id="embed_server">
             <h3>Add badge to your website</h3>
             <div class="code">
                 ${badgeCode}
             </div>
         </div>
     </div>
</@page.page>