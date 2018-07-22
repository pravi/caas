<#ftl output_format="HTML">
<#import "mail.ftl" as mail>
<#assign preheader = "Error running XMPP Compliance Tester for ${domain}" in mail/>
<@mail.mail>
<p>
    We are facing errors running XMPP Compliance Tester for ${domain} .
    Make sure that your server is up, and is working correctly.
</p>
<p style="color:red;">
    We encountered error while running test #${iteration.getIterationNumber()}.
</p>

<p>
    The test ran ${timeSince}
    <br>
    (from ${iteration.getBegin()} GMT to ${iteration.getEnd()} GMT)
</p>
</p>
</@mail.mail>

