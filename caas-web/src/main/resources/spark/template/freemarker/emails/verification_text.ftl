<#ftl output_format="plainText">
<#import "mail_text.ftl" as mail>
<@mail.mail>
To verify your email ID for receiving important alerts and reports from XMPP Compliance Tester about ${domain}, go to ${rootUrl}/confirm/${code}/
This link will expire within 24 hours.
</@mail.mail>
