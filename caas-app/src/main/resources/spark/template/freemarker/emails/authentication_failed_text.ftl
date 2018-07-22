<#ftl output_format="plainText">
<#import "mail_text.ftl" as mail>
<@mail.mail>
We could not authenticate ${credential.getJid()} for ${domain} . So, we removed it from our credentials list.
You can check if there are any credentials left for ${domain}, by going to ${rootUrl}/live/${domain}
If there are no credentials left, add new credentials for ${domain} by going to ${rootUrl}/add/
</@mail.mail>

