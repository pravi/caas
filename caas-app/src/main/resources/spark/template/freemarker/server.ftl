<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#assign description="Server compliance result for ${domain}" in page>
<#assign title="${page.project_name}: Compliance result for ${domain}" in page>
<@page.page>
    <script src="https://d3js.org/d3.v5.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script>
        "use strict";
        var drawGraph = function () {
            var data = JSON.parse('${historic_data?no_esc}');
            var utils = {
                "isMobile": function () {
                    var check = false;
                    (function (a) {
                        if (/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino/i.test(a) || /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0, 4))) check = true;
                    })(navigator.userAgent || navigator.vendor || window.opera);
                    return check;
                },
                "formatDate": function (date) {
                    var hour = date.getHours();
                    var min = date.getMinutes();
                    var monthNames = [
                        "January", "February", "March",
                        "April", "May", "June", "July",
                        "August", "September", "October",
                        "November", "December"
                    ];
                    var day = date.getDate();
                    var monthIndex = date.getMonth();
                    var year = date.getFullYear();
                    return day + ' ' + monthNames[monthIndex] + ' ' + year + ',  ' + hour + ':' + min;
                },
                "getPointRadius": function (hovered) {
                    var r;
                    if (utils.isMobile()) {
                        r = 10;
                        if (hovered) {
                            r = 16;
                        }
                    } else {
                        r = 7;
                        if (hovered) {
                            r = 13;
                        }
                    }
                    return r;
                }
            };

            if (data.length < 2) {
                d3.select("#chart_container").html("<h2>We donot have enough data to show historical view</h2>");
            }
            var times = [];
            for (var i in data) {
                var d = data[i];
                var x = new Date(d.timestamp);
                d.timestamp = x;
                times.push(x);
            }

            for (var i in data) {
                var d = data[i];
                var html = "<b>" + d.passed + "/" + d.total + " (" + (d.passed / d.total * 100).toFixed(2) + "%) tests passed</b>";
                if (utils.isMobile()) {
                    html += "<button>See report</button>";
                }
                html += "<p>Time: " + utils.formatDate(d.timestamp) + "</p>";
                if ("change" in d) {
                    if ("pass" in d.change && d.change.pass.length !== 0) {
                        html += "<p>Changes in results:</p>";
                    }
                    else if("fail" in d.change && d.change.fail.length !== 0) {
                        html += "<p>Changes in results:</p>";
                    }

                    if ("pass" in d.change && d.change.pass.length !== 0) {
                        html += "<div class='chart_tests_pass'>Passing in :<br><ul>"
                        for (var i in d.change.pass) {
                            var a = d.change.pass[i];
                            html += "<li>" + a + "</li>";
                        }
                        html += "</ul></div>"
                    }
                    if ("fail" in d.change && d.change.fail.length !== 0) {
                        html += "<div class='chart_tests_fail'>Failing in :<ul>"
                        for (var i in d.change.fail) {
                            var a = d.change.fail[i];
                            html += "<li>" + a + "</li>";
                        }
                        html += "</ul></div>"
                    }
                }
                d.tooltipHTML = html;
            }

            var width = 5000;
            var ticks = 50;
            var height = 500;
            var marginRL = 70;
            var marginUD = 30;
            var extent = d3.extent(times);
            var days = (extent[1] - extent[0]) / (1000 * 60 * 60 * 24);

            if (days > 3650) {
                width = days;
                ticks = 50;
            }
            else if (days > 2000) {
                width = 1.5 * days;
                ticks = 40;
            }
            else if (days > 1000) {
                width = 2 * days;
                ticks = 20;
            }
            else if (days > 365) {
                width = 3 * days;
                ticks = 15;
            }
            else if (days > 100) {
                width = 5 * days;
                ticks = 10;
            }
            else if (days > 60) {
                width = 10 * days;
                ticks = 6;
            } else {
                width = 600;
                ticks = 5;
            }
            var svg = d3.select("#svg_container");
            svg.attr('width', width + 'px');
            svg.attr('height', height + 'px');
            var yScale = d3.scaleLinear().domain([0, 100]).range([(height - marginUD), marginUD]);
            var xScale = d3.scaleTime().domain(extent)
                    .range([marginRL, (width - marginRL)]);

            var linepoints = [];
            for (var k in data) {
                if (k > 0) {
                    var addx = xScale(data[k].timestamp);
                    var addy = yScale(data[k - 1].passed * 100 / data[k - 1].total);
                    linepoints.push({x: addx, y: addy});
                }
                linepoints.push({x: xScale(data[k].timestamp), y: yScale(data[k].passed * 100 / data[k].total)})
            }
            var line = d3.line()
                    .x(function (d) {
                        return d.x;
                    })
                    .y(function (d) {
                        return d.y;
                    });

            var tooltip = d3.select("#tooltip");
            if(!utils.isMobile()) {
                tooltip.style('pointer-events', 'none');
            }
            var xAxis = d3.axisBottom().scale(xScale).ticks(ticks);
            var yAxis = d3.axisLeft().scale(yScale).ticks(10)
                    .tickSize(-(width - 2 * marginRL), 0, 0);

            svg.append("g")
                    .attr("transform", "translate(0," + ((height - marginUD)) + ")")
                    .call(xAxis);
            svg.append("g").attr("transform", "translate(" + marginRL + ",0)")
                    .call(yAxis);

            //Add circles
            svg.append("path")
                    .attr("d", line(linepoints))
                    .attr("stroke", "black")
                    .attr("stroke-width", 3)
                    .attr("fill", "none");

            function gotoHistoric(domain, iteration) {
                var url = window.location.protocol + "//" + location.hostname + ":" + location.port + "/historic/server/" + domain + "/iteration/" + iteration;
                window.location = url;
            }

            svg.append("g").attr("id", "historical_graph");

            svg.append("text")
                    .attr("class", "axis_label")
                    .attr("text-anchor", "end")
                    .attr("x", -(height - 2 * marginUD) / 2)
                    .attr("y", marginRL / 4)
                    .attr("dy", ".75em")
                    .attr("transform", "rotate(-90)")
                    .text("Compliance percentage");

            var circle = d3.select("#historical_graph").selectAll("circle").data(data);

            var inCircle = circle.enter();

            inCircle.append("circle")
                    .attr("class", "historic_point " + (utils.isMobile() ? "mobile" : ""))
                    .attr('r', utils.getPointRadius(false))
                    .attr('cx', function (d, i) {
                        var x = xScale(d.timestamp);
                        return x;
                    })
                    .style('fill', 'white')
                    .attr('stroke', '#43a047')
                    .attr('stroke-width', 3)
                    .attr('cy', function (d, i) {
                        var y = yScale(d.passed * 100 / d.total);
                        return y;
                    })
                    .on("mouseout", function (d) {
                        d3.select(this).transition().attr('r', utils.getPointRadius(false));
                        if (utils.isMobile()) {
                            tooltip.transition().style('visibility', 'hidden')
                        }
                        return tooltip.transition().style('opacity', 0);
                    })
                    .on("mouseover", function (d) {
                        d3.select(this).transition().attr('r', utils.getPointRadius(true));
                        tooltip.html(d.tooltipHTML);
                        if (utils.isMobile()) {
                            tooltip.transition().style('visibility', 'visible')
                        }
                        return tooltip.transition().style('opacity', 1)
                    })
                    .on("click", function (d) {
                        if (utils.isMobile()) {
                            var it = d.iteration;
                            tooltip.select("button")
                                    .on("click", function () {
                                        gotoHistoric("${domain}", d.iteration);
                                    });
                        }
                        else {
                            gotoHistoric("${domain}", d.iteration);
                        }
                    })
                    .on("mousemove", function (d, i) {
                                var realWidth = window.innerWidth;
                                var toolTipWidth = parseInt(tooltip.style("width"));
                                var paddingLeft = parseInt(tooltip.style("padding-left"));
                                var marginLeft = parseInt(tooltip.style("margin-left"));
                                var paddingRight = parseInt(tooltip.style("padding-right"));
                                var marginRight = parseInt(tooltip.style("margin-right"));
                                var space = marginRight + paddingRight + marginLeft + paddingLeft;
                                var tooltipX = d3.event.pageX;
                                if (tooltipX > (realWidth - toolTipWidth - space)) {
                                    tooltipX = realWidth - toolTipWidth - space;
                                }

                                var removedFrom = 0;
                                return tooltip.style("top", (d3.event.pageY + 10) + "px")
                                        .style("left", tooltipX + "px");
                            }
                    );
        };
    </script>
    <style>
        #tooltip {
            position: absolute;
            opacity: 0;
            top: 0px;
            width: 300px;
            z-index: 100;
        }


        .chart_tests_fail {
            color: #bb1111;
            float: right;
        }

        .chart_tests_fail *, .chart_tests_pass * {
            margin: 0px;
        }

        .chart_tests_pass {
            color: #43a047;
            float: left;
        }

        .tick > line {
            stroke: #aaaaaa;
        }

        .historic_point {
            cursor: pointer;
        }

        #chart_container {
            position: relative;
            margin: 5px;
            padding: 5px;
            overflow-x: scroll;
        }

        #server_stats {
            display: flex;
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
            drawGraph();
        });
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

    <button id="download_report" onclick="print_report()">Download report</button>
    <br><br>
    <#if softwareName??>
        Server is running ${softwareName} ${softwareVersion!}
    <#else>
        Server is running unknown software
    </#if>

    <br><br>
     <div class="card" id="chart_container">
         <svg id="svg_container" shape-rendering="optimizeQuality" preserveAspectRatio="xMinYMin">
         </svg>
     </div>

    <div class="card" id="tooltip"></div>

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