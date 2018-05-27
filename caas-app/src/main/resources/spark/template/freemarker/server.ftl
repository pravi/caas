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


        .input_subscribe {
            width: 250px;
        }

        #help_server {
            display: inline-flex;
            flex-wrap: wrap;
            justify-content: space-between;
            margin: auto 20px;
        }

        .help {
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
                <div class="server_result chip server_failed"
                     onclick="window.location='#${result.getTest().short_name()}'">
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

        <h3>
            Instructions on passing the failed tests
        </h3>
        <div id="help_server">
        <#list helps as help>
            <#if help.isPossible()??>
              <div class="help card" id="${help.getName()}">
                  <h3>For <a href="/tests/${help.getName()}">${help.getName()}</a>:</h3>
                  <ol>
                      <#if help.getSince()??>
                          <li>
                              Make sure your server is at least ${help.getSince()}
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
                                  ${help.getInstructions()}
                              </li>
                          </div>
                      </#if>
                  </ol>
              </div>
            </#if>
        </#list>
        </div>
</@page.page>