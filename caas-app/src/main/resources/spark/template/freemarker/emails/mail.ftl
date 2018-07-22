<#import "unsubscribe.ftl" as unsubscribe>
<#macro mail>
<div class="email_background"
     style="background: #eee;padding: 20px;color: #333;">
    <div class="pre-header" style="color:#ccc;display: none;font-size: 10px;">
        ${(preheader)!}
    </div>
    <div class="email-container"
         style="text-align: center;background:white;padding: 20px; margin: 0px auto;font-size: 16px;font-family: sans-serif;font-weight: 300;max-width: 500px;">
        <h1>XMPP Compliance Tester</h1>
<#nested>
    </div>
    <#if subscriber??>
        <@unsubscribe.unsubscribe>
        </@unsubscribe.unsubscribe>
    </#if>
</div>
</#macro>
