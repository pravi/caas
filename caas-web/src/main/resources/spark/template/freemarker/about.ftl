<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#assign title="About XMPP Compliance Tester | ${page.project_name}" in page>
<#assign stylesheets=["/css/about.css"] in page>
<@page.page>
    <h2>
        About XMPP Compliance Tester
    </h2>
    <link rel="stylesheet" href="/css/about.css">

        <p>
            This is a web service for checking and visualising compliance status of XMPP servers, with XEPs (XMPP
            Extension Protocols), made as a part of
            <a href="https://summerofcode.withgoogle.com/projects/5341326460059648">Google Summer of Code 2018</a> for Conversations.im by <a href="https://rishiraj.me">Rishi Raj</a>.
            The code for this website, along with a command line tool to check compliance of your server locally, is available under a BSD-3 License on <a href="https://github.com/iNPUTmice/caas">Github</a>.
        </p>
    <div class="card">
         <h3>Why compliance?</h3>
         <p>
             XMPP is an extensible and living standard. Requirements shift over time and thus new extensions (called
             XEPs) get developed.
             While server implementors usually react quite fast and are able to cater to those needs it's the server
             operators who don't upgrade to the latest version or don't enable certain features.
         </p>

         <p class="sub_heading">For users:</p>
         <p>
             As a user, it is not easy to choose a good XMPP server for creating Jabber ID.
             Using this web service you can choose a good quality server by comparing
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

     <div class="card">
         <h3>Features</h3>
         <ul>
             <li>
                 Tests servers regularly for compliance with XEPs
             </li>
             <li>
                 Sends alerts to subscribers about invalid credentials, errors occuring while running tests
                 and changes in results for servers i.e. if there is a change in a test's success for that server
             </li>
             <li>
                 Generates badges showing the compliance of servers with XEPs
             </li>
             <li>
                 Helps server admins/maintainers getting the right configuration for their servers by giving them information on passing failed tests
             </li>
             <li>
                 Allows users to explore historical compliance for servers and tests by clicking their links and clicking points on
                 graphs
             </li>
         </ul>
     </div>

</@page.page>