<#import "mail_text.ftl" as mail>
<@mail.mail>
We are facing errors running XMPP Compliance Tester for ${domain}. Make sure that your server is up, and is working correctly.
We encountered error while running test #${iteration.getIterationNumber()}. You can check the test result by going to ${rootUrl}/historic/server/${domain}/iteration/${iteration.getIterationNumber()}/
The test ran ${timeSince} (from ${iteration.getBegin()} GMT to ${iteration.getEnd()} GMT)
</@mail.mail>
