<#ftl output_format="HTML">
<#import "mail.ftl" as mail>
<#assign preheader = "Error running XMPP Compliance Tester for ${domain}" in mail/>
<@mail.mail>
<p>We have added a new credential ${credential.getJid()} for ${domain} successfully.</p>
</@mail.mail>
