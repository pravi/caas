<#ftl output_format="HTML">
<#import "mail.ftl" as mail>
<#assign preheader = "Verify your email ID to receive alerts and reports from XMPP Compliance Tester about ${domain}" in mail/>
<@mail.mail>
<p>
    To verify your email ID for receiving important alerts and reports from XMPP Compliance Tester about ${domain}, go
    to
    <a href="${rootUrl}/confirm/${code}/">${rootUrl}/confirm/${code}/</a> .
This link will expire within 24 hours.
</p>
</@mail.mail>

