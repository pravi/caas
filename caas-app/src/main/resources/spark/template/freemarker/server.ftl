<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#import "graph.ftl" as graph>
<#import "stat.ftl" as stat>
<#assign description="Server compliance result for ${domain}" in page>
<#assign title="${page.project_name}: Compliance result for ${domain}" in page>
<@page.page>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <style>

        #server_results {
            padding-top: 20px;
            padding-bottom: 20px;
        }

        #server_run {
            padding: 30px;
        }

        #additional_server {
            margin: 50px auto;
            display: inline-flex;
            justify-content: space-between;
        }

        .input_subscribe {
            width: 250px;
        }

        #help_container {
            display: block;
            visibility: hidden;
            position: fixed;
            z-index: 1;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgb(0, 0, 0);
            background-color: rgba(0, 0, 0, 0.4);
        }

        .help {
            margin-top: ${1.3*page.nav_bar_height}px;
            overflow: auto;
            display: none;
            text-align: left;
        }

        .close {
            color: #f44;
            float: right;
            font-size: 40px;
            font-weight: bold;
            cursor: pointer;
        }

        @media only screen and (max-width: 850px) {
            #additional_server {
                display: block;
            }
        }
    </style>
    <script>
        var print_report = function () {
            var printWindow = window.open('', 'PRINT', 'height=400,width=600');
            printWindow.document.write('<html><head><title>' + document.title + '</title>');
            printWindow.document.write('<style>');
            printWindow.document.write(".server_passed::before { color: green; content: 'PASSED: '; }");
            printWindow.document.write(".server_failed::before { color: red; content: 'FAILED: '; }");
            printWindow.document.write("h1,h2,h3 { text-align: center; }");
            printWindow.document.write('</style>');
            printWindow.document.write('</head>');
            printWindow.document.write('<body>');
            printWindow.document.write('<h1>' + document.title + '</h1>');
            printWindow.document.write('<h2>' + 'Tests ran on ${timestamp}' + '</h2>');
            <#if softwareName??>
                printWindow.document.write('<h3>' + 'Server is running ${softwareName} ${softwareVersion!}' + '</h3>');
            <#else>
                printWindow.document.write('<h3>' + 'Server is running unknown software' + '</h3>');
            </#if>
            printWindow.document.write('<h2>' + 'Test results' + '</h2>');

            $("#server_results").each(function (i, obj) {
                printWindow.document.write(obj.innerHTML);
            });

            printWindow.print();
            printWindow.close();

            return true;
        };
        var shown;
        var preventModalClosing = function (e) {
            e.stopPropagation(); // this stops the event from bubbling up to the doucment
        };

        function showHelp(name) {
            shown = $('#' + name);
            shown.css("display", "block");
            $('#help_container').css("visibility", "visible");
            $(".close").click(close_modal);
        }

        function close_modal() {
            if (shown) {
                shown.css("display", "none");
            }
            $('#help_container').css("visibility", "hidden");
        }

        $(function () {
            //Close modal on clicking anywhere
            $("#content").click(close_modal);
            //Prevent closing of modal when clicking inside help card or chip
            $(".help").click(preventModalClosing);
            $(".chip").click(preventModalClosing);
            var data = JSON.parse('${historic_data?no_esc}');
            drawGraph(data, function gotoHistoric(d) {
                var url = window.location.protocol + "//" + location.hostname + ":" + location.port + "/historic/server/${domain}/iteration/" + d.iteration;
                window.location = url;
            })
        });
    </script>

    <h2> Compliance status for ${domain}</h2>

    <@stat.stat></@stat.stat>

   <button id="download_report" onclick="print_report()">Download report</button>
    <br><br>
    <#if softwareName??>
        Server is running ${softwareName} ${softwareVersion!}
    <#else>
        Server is running unknown software
    </#if>

    <br><br>

    <@graph.graph>
    </@graph.graph>

    <div id="server_results">
        <#list results as result>
            <#if result.success>
                <div class="chip passed">
            <#else>
                <div class="chip clickable failed"
                     onclick="showHelp('${result.getTest().short_name()}')">
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
                <form id="form_subscribe" action="#subscribe" method="post">
                    <div>
                        <label for="email" class="input_label_subscribe">E-Mail</label>
                        <input name="email" class="input_subscribe" type="text"/>
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

        <#if helps??>
            <div id="help_container">
                <#list helps as help>
                    <#if help.isPossible()??>
                      <div class="help card" id="${help.getName()}">
                          <div class="close">&times;</div>
                          <h3>For <a href="/test/${help.getName()}">${help.getName()}</a>:</h3>
                          <ul>
                              <#if help.getSince()??>
                                  <li>
                                      Make sure your server is at least ${help.getSince()}
                                      <#if softwareVersion??>(currently ${softwareVersion})</#if>
                                  </li>
                              </#if>
                              <#if help.modulesRequired??>
                                  <li>
                                      Add the following modules to your configuration file:
                                      <div class="modules">
                                         <#list help.getModulesRequired() as module>
                                             <div class="module">
                                                 <div class="module_name ${module.getType()}">
                                                     <#if module.getLink()??>
                                                         <a href="${module.getLink()}">
                                                             ${module.getName()}
                                                         </a>
                                                     <#else>
                                                         ${module.getName()}
                                                     </#if>

                                                     <#if module.getType() == "community_prosody">
                                                         <br><br>
                                                         This module doesn't come with Prosody installation.
                                                         You will have to download it by following
                                                         <a href="https://prosody.im/doc/installing_modules">
                                                             these instructions
                                                         </a>
                                                     </#if>
                                                 </div>
                                             </div>
                                         </#list>
                                      </div>
                                  </li>
                              </#if>
                              <#if help.getInstructions()??>
                                  <div class="instructions">
                                      <li>
                                          ${help.getInstructions()?no_esc}
                                      </li>
                                  </div>
                              </#if>
                          </ul>
                      </div>
                    </#if>
                </#list>
            </div>
        </#if>
</@page.page>