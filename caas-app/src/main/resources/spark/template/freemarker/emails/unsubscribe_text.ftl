<#ftl output_format="plainText">
<#macro unsubscribe>
Unsubscribe from receing alerts about ${subscriber.getDomain()} by going to ${rootUrl}/unsubscribe/${subscriber.unsubscribeCode}
</#macro>