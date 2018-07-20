<#macro mail>
<!DOCTYPE html>
<html>
<head>
    <style>
        #bottom {
            color #222222:
            background-color: #c3c3c3;
        }
    </style>

</head>
<body>
<#nested>
<div id="bottom">
    <a href="${rootUrl}/unsubscribe/${subscriber.unsubscribeCode}">Unsubscribe</a> from receiving alerts, periodic compliance reports for ${subscriber.domain}
</div>
</body>

</html>
</#macro>
