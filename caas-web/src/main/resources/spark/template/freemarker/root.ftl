<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#import "includes/table.ftl" as table>
<#assign scripts=["/js/vue.min.js","/js/search_bar.js"] in page>
<#assign stylesheets=["/css/search_bar.css"] in page>
<#assign description="Pick and choose your Jabber server from a list of compatible servers or check if your current server supports all required features." in page>
<#assign title="Server overview &middot; ${page.project_name}" in page>
<#assign no_results_found_msg="No results found. <a href='/add'>Add</a> credentials for some XMPP servers to get started">
<#assign footerText="<a href='/old/'>Old table view</a>" in page>
<@page.page>
    <h1>Check your server for compliance</h1>
    <div id="search_box">
        <div id="search_suggestion_box">
            <div id="input_div">
                <input id="search_field" v-model="filter" @keyup.enter="enter"/>
                <div id="search_submit" @click="enter">Submit</div>
            </div>
            <div class="suggestions">
                <div class="suggestion_item" @click="go" v-for="server,index in displayServers">{{server}}</div>
                <div class="suggestion_item" @click="add" v-show='filter != null && filter != ""'>Add {{filter}}</div>
            </div>
        </div>
    </div>

    <script>
        createVueApp(JSON.parse('${servers?no_esc}'));
    </script>
</@page.page>