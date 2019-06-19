<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#import "includes/table.ftl" as table>
<#assign scripts=["/js/vue.min.js","/js/search_bar.js"] in page>
<#assign stylesheets=["/css/search_bar.css"] in page>
<#assign description="Pick and choose your Jabber server from a list of compatible servers or check if your current server supports all required features." in page>
<#assign title="Server overview &middot; ${page.project_name}" in page>
<#assign no_results_found_msg="No results found. <a href='/add'>Add</a> credentials for some XMPP servers to get started">
<@page.page>
    <h1>Check your server for compliance</h1>
    <div id="search_box">
        <div id="search_submit" @click="enter">Go</div>
        <div id="search_suggestion_box">
            <div id="input_div">
                <input id="search_field" v-on:keyup.up="selectionUp" v-on:keyup.down="selectionDown" v-model="filter" @keyup.enter="enter"/>
            </div>
            <div class="suggestions">
                <div class="suggestion_item" v-for="server,index in displayServers" v-bind:class="{selected: selected==index}" @mouseover="setSelection(index)" @click="go" v-show='filter != null && filter != ""'>{{server}}</div>
                <div class="suggestion_item" @mouseover="selectAdd()" v-bind:class="{selected: selected == displayServers.length}" @click="add" v-show='filter != null && filter != ""'>Add {{filter}}</div>
            </div>
        </div>
    </div>
    <#if recommendations?has_content>
        <div id="recommended_servers">
        <h3>Randomly suggested compliant servers</h3>
        <#list recommendations as recommendation>
            <div class="chip clickable" onclick="location.href='/server/${recommendation}'">
            ${recommendation}
            </div>
        </#list>
        </div>
    </#if>
    <script>
        createVueApp(JSON.parse('${servers?no_esc}'));
    </script>
</@page.page>