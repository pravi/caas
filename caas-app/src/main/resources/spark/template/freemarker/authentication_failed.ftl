<#ftl output_format="HTML">
<#import "mail.ftl" as mail>
<#assign preheader = "" in mail/>
<@mail.mail>
<p style="color:red;">
    We could not authenticate ${credential.getJid()} for ${domain} . So, we removed it from our credentials list.
</p>

<p>
    You can check if there are any credentials left for ${domain},
    by going to the <a href="${rootUrl}/live/${domain}">live result page</a>.
    If there are no credentials left, add new credentials for ${domain}
    by going to <a href="${rootUrl}/add/">add page</a> .
</p>
</@mail.mail>

