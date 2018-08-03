<#ftl output_format="HTML">
<#import "mail.ftl" as mail>
<#assign preheader = "" in mail/>
<@mail.mail>
<p style="color:red;">
    We could not authenticate ${credential.getJid()} for ${domain} . So, we removed it from our credentials list.
</p>

<p>
    Please add new credentials for ${domain}
    by going to <a href="${rootUrl}/add/">add page</a> to keep running these tests,
    and prevent receiving emails about test failures.
</p>
</@mail.mail>

