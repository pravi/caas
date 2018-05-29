<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#assign description="Server compliance result for ${domain}" in page>
<#assign title="${page.project_name}: Compliance result for ${domain}" in page>
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
            color: ${page.green};
            font-size: 3em;
        }

        #server_results {
            padding-top: 20px;
            padding-bottom: 20px;
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
            padding-top: ${page.nav_bar_height};
            overflow: auto;
            background-color: rgb(0, 0, 0);
            background-color: rgba(0, 0, 0, 0.4);
        }

        .help {
            text-align: left;
        }

        .close {
            color: #f44;
            float: right;
            font-size: 40px;
            font-weight: bold;
            cursor: pointer;
        }

        @media only screen and (max-width: 8px) {
            #additional_server {
                display: block;
            }
        }
    </style>
    <script>
        var shown;
        var preventModalClosing = function (e) {
            e.stopPropagation(); // this stops the event from bubbling up to the doucment
        };
        $(function () {
            var helpContainer = $("#help_container");
            var help = $(".help");
            helpContainer.css("visibility", "visible");
            help.hide();
            helpContainer.hide();

            //Close modal on clicking anywhere
            $("#content").click(close_modal);
            //Prevent closing of modal when clicking inside help card or chip
            help.click(preventModalClosing);
            $(".chip").click(preventModalClosing);
        });

        function showHelp(name) {
            shown = $('#' + name);
            shown.show();
            $('#help_container').show();
            $(".close").click(close_modal);
        }

        function close_modal() {
            $('#help_container').hide();
            shown.hide();
        }

    </script>

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
                <div class="server_result chip clickable server_failed"
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
                          <h3>For <a href="/tests/${help.getName()}">${help.getName()}</a>:</h3>
                          <ol>
                      <#if help.getSince()??>
                      <li>
                          Make sure your server is at least ${help.getSince()}
                          (currently <#if softwareVersion??>${softwareVersion}</#if>)
                      </li>
                      </#if>
                      <#if help.modulesRequired??>
                      <li>
                          Add the following modules to your configuration file:
                          <div class="modules">
                              <br>
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
                      <br>
                      This module doesn't come with Prosody installation.
                      <br>
                      You will have to download it by following <a
                              href="https://prosody.im/doc/installing_modules">these
                          instructions</a>
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
                          </ol>
                      </div>
                    </#if>
                </#list>
            </div>
        </#if>
</@page.page>