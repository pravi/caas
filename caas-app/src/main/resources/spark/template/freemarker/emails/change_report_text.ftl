<#ftl output_format="plainText">
<#import "mail_text.ftl" as mail>
<@mail.mail>

While running test #${iteration.getIterationNumber()} ,
we recorded the following changes in the compliance status for ${subscriber.domain}:

<#if change.getPass()?has_content>
Passing in:
<#list change.getPass() as pass>
${tests[pass].full_name()}
</#list>
</#if>

<#if change.getFail()?has_content>
Failing in:
<#list change.getFail() as fail>
${tests[fail].full_name()}
</#list>

</#if>
More details on ${rootUrl}/server/${subscriber.domain}
This test ran from ${iteration.getBegin()} GMT to ${iteration.getEnd()} GMT
</@mail.mail>

