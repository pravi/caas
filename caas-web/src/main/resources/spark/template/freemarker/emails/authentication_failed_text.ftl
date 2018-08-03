<#ftl output_format="plainText">
<#import "mail_text.ftl" as mail>
<@mail.mail>
We could not authenticate ${credential.getJid()} for ${domain} . So, we removed it from our credentials list.
Please add new credentials for ${domain} by going to ${rootUrl}/add/ to keep running these tests,
and prevent receiving emails about test failures.
</@mail.mail>

