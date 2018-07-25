<#import "mail_text.ftl" as mail>
<@mail.mail>
We are facing errors running XMPP Compliance Tester for ${domain}.
We encountered error while running test #${iteration.getIterationNumber()}. You can check the test result by going to ${rootUrl}/historic/server/${domain}/iteration/${iteration.getIterationNumber()}/
The test ran ${timeSince} (from ${iteration.getBegin()} GMT to ${iteration.getEnd()} GMT)
Make sure that your server is up, and is working correctly. Also ensure that the credentials supplied are correct.
You can check if your server is working correctly and the credentials are correct by going to ${rootUrl}/live/${domain}
</@mail.mail>
