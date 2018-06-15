<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#assign description="Get detailed report for your XMPP servers' compliance test results, periodic reports." in page>
<#assign title="${page.project_name}: Test your XMPP server for compliance" in page>
<#assign stylesheets=["/css/about.css"] in page>
<@page.page>
    <h2>
        About Compliance as a Service (CaaS)
    </h2>
    <link rel="stylesheet" href="/css/about.css">

        <p>
            This is a work in progress web application based off
            <a href="https://github.com/iNPUTmice/ComplianceTester">XMPP Compliance tester</a>
            for checking and visualising compliance status of various XMPP servers,
            made as a part of Google Summer of Code 2018 for Conversations.im by
            <a href="https://rishiraj22.github.io">Rishi Raj</a>.
        </p>
     <div class="card">
         <h3>Why compliance?</h3>
         <p>
             XMPP is an extensible and living standard. Requirments shift over time and thus new extensions (called
             XEPs) get developed.
             While server implementors usually react quite fast and are able to cater to those needs it's the server
             operators who don't upgrade to the latest version or don't enable certain features.
         </p>

         <p class="sub_heading">For users:</p>
         <p>
             As a user, it is not easy to choose a good XMPP server for creating Jabber ID.
             Using this web application you can choose a good quality server by comparing
             the servers which have implemented the latest specifications and which servers have been
             fast at implementing new specifications historically.
         </p>

         <p class="sub_heading">For server admins:</p>
         <p>
             Picking the right extensions to implement or enable isn't always easy. For this reason the XSF has
             published <a href="https://xmpp.org/extensions/xep-0387.html">XEP-0387 XMPP Compliance Suites 2018</a>
             listing the most important extensions to date.
             This app won't just help you to assess if your server supports those compliance profiles(and more), but
             also give you some instructions on how to implement the profiles which are currently not supported.
         </p>
     </div>

</@page.page>