<#ftl output_format="HTML">
<#import "mail.ftl" as mail>
<#assign preheader = "While running test #${iteration.getIterationNumber()}, we recorded some changes in the compliance report for ${subscriber.domain}" in mail/>
<@mail.mail>
    <p>
        While running test
        <a href="${rootUrl}/historic/server/${subscriber.domain}/iteration/${iteration.getIterationNumber()}/">
            #${iteration.getIterationNumber()}
        </a>
        , we recorded the following changes in the compliance status for ${subscriber.domain}:
    </p>
    <#if change.getPass()?has_content>
        <h3>
            Passing in:
        </h3>
        <ul style="color: #43a047">
            <#list change.getPass() as pass>
                <li style="list-style-position: inside;">
                    ${tests[pass].full_name()}
                </li>
            </#list>
        </ul>
    </#if>
    <#if change.getFail()?has_content>
        <h3>
            Failing in:
        </h3>
        <ul style="color: #ff4444">
        <#list change.getFail() as fail>
            <li style="list-style-position: inside;">
                ${tests[fail].full_name()}
            </li>
        </#list>
        </ul>
    </#if>
    <div class="cta"
         style="display: inline-block;background: #43a047;font-size: 24px;padding: 10px 20px;border-radius: 5px;">
        <a href="${rootUrl}/server/${subscriber.domain}"
           style="text-decoration: none;color: white;padding: 5px 10px;">More
            details</a>
    </div>
    <p>
        This test ran from ${iteration.getBegin()} GMT to ${iteration.getEnd()} GMT
    </p>
</@mail.mail>

