<#assign project_name="CaaS">
<#macro page>
    <#assign nav_bar_height=80>
    <#assign primary_color="#43a047">
    <#assign background_color="#fafafa">
    <#assign white="#ffffff">
    <#assign green="#43a047">
<html>
<head>
    <style>
        body, #content {
            color: rgba(0, 0, 0, 0.87);
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
            font-size: 13pt;
            background-color: ${background_color};
        }

        #content {
            margin-top: ${1.1*nav_bar_height}px;
            text-align: center;
        }

        input {
            font-size: 0.9em;
            background: none;
            text-decoration: none;
            outline: none !important;
            border: none;
            border-bottom: solid 2px ${primary_color};
        }

        a {
            color: ${primary_color};
        }

        .button, button {
            font-size: 0.95em;
            background: ${primary_color};
            color: white;
            box-shadow: 0 3px 6px 0 rgba(0, 0, 0, 0.2);
            border: none;
            cursor: pointer;
            text-align: center;
            border-radius: 8px;
            margin-top: 10px;
            padding: 10px;
        }

        .button:disabled, button:disabled {
            cursor: not-allowed;
        }

        .button:hover, button:hover {
            background: ${white};
            color: ${primary_color};
            box-shadow: 0 3px 6px 0 rgba(0, 0, 0, 0.2);
            -webkit-transition: all 0.3s;
            -transition: all 0.3s;
            -ms-transition: all 0.3s;
            -o-transition: all 0.3s;
            transition: all 0.3s;
        }

        .card {
            padding: 20px;
            margin: 15px 10px;
            background: ${white};
            box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2);
        }

        .chip {
            display: inline-flex;
            padding: 10px;
            background: ${white};
            border-radius: 10px;
            box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2);
            margin: 10px;
        }

        code, .code {
            padding: 5px;
            font-size: 0.8em;
            text-align: left;
            font-family: monospace;
        }

        footer {
            margin-top: 30px;
            clear: both;
            display: block;
            width: 100%;
            text-align: center;
        }

        /*
            Begin navbar CSS
        */
        #nav_bar a {
            text-decoration: none;
        }

        #nav_bar {
            position: absolute;
            top: 0px;
            right: 0px;
            left: 0px;
            background: ${primary_color};
            z-index: 200;
            height: ${nav_bar_height}px;
        }

        #nav_bar * {
            color: ${white};
        }

        #brand {
            float: left;
            margin: 0px;
            margin-left: 20px;
            height: ${nav_bar_height}px;
        }

        #nav_list_menu {
            float: right;
            display: -webkit-box;
            display: -ms-flexbox;
            display: -webkit-inline-box;
            display: -ms-inline-flexbox;
            display: inline-flex;
            height: ${nav_bar_height}px;
        }

        #nav_list_menu a {
            padding: 15px;
            margin: auto;
            position: relative;
            text-align: center;
            font-family: 'Ubuntu', sans-serif;
            font-size: 1.7em;
            -webkit-transition: all 300ms ease-in-out;
            transition: all 300ms ease-in-out;
        }

        #nav_list_menu a:hover {
            background-color: ${background_color};
            color: ${primary_color};
            -webkit-transition: all 300ms ease-in-out;
            transition: all 300ms ease-in-out;
        }

        #hamburger {
            float: right;
            display: -webkit-box;
            display: -ms-flexbox;
            display: flex;
            -webkit-box-orient: vertical;
            -webkit-box-direction: normal;
            -ms-flex-direction: column;
            flex-direction: column;
            height: ${nav_bar_height}px;
            padding: 5px;
            margin-right: 10px
        }

        #hamburger span {
            -webkit-transition: -webkit-transform 500ms;
            transition: -webkit-transform 500ms;
            transition: transform 500ms;
            transition: transform 500ms, -webkit-transform 500ms;
        }

        #hamburger.clicked span:nth-child(3) {
            -webkit-transform: translate(-10px, 0px) rotate(45deg) translate(0px, -28px);
            transform: translate(-10px, 0px) rotate(45deg) translate(0px, -28px);
        }

        #hamburger span:nth-child(2) {
            -webkit-transition: opacity 400ms;
            transition: opacity 400ms;
        }

        #hamburger.clicked span:nth-child(2) {
            opacity: 0;
        }

        #hamburger.clicked span:nth-child(1) {
            -webkit-transform: translate(-10px, 0px) rotate(-45deg) translate(0px, 28px);
            transform: translate(-10px, 0px) rotate(-45deg) translate(0px, 28px);
        }

        @media only screen and (max-width: 850px) {

            #nav_list_menu {
                display: block;
                visibility: hidden;
                opacity: 0;
                clear: both;
                margin: 0px;
                padding: 0px;
                background-color: ${primary_color};
                color: ${background_color};
                width: 100%;
                transition: visibility 0s, opacity 0.3s ease-in-out;
                height: auto;
            }

            #nav_list_menu a {
                margin: 0px;
                top: 0px;
                padding: 10px 0px;
                height: auto;
                clear: both;
                display: block;
                width: 100%;
            }

            #nav_list_menu.expanded {
                opacity: 1;
                visibility: visible;
            }

            #nav_list_menu.expanded a {
                display: block;
            }

            #hamburger {
                display: visible;
            }

            #hamburger {
                width: 70px;
                height: 65px;
                display: inline;
                margin: 0px;
                margin-right: 10px;
                padding: 0px;
                padding-top: 14px;
            }

            #hamburger span {
                display: block;
                margin-bottom: 10px;
                background-color: ${white};
                height: 10px;
                width: 55px;
            }
        }

        .loading_status, .loading_status > * {
            font-size: 1.3em;
            display: inline;
            line-height: 70px;
            vertical-align: middle;
        }

        .loader {
            border: 9px solid #f3f3f3;
            border-top: 9px solid ${primary_color};
            border-radius: 50%;
            display: inline-block;
            width: 30px;
            height: 30px;
            animation: spin 2s linear infinite;
        }

        @keyframes spin {
            0% {
                transform: rotate(0deg);
            }
            100% {
                transform: rotate(360deg);
            }
        }
    </style>
    <meta property="og:title" content="${title!"Check your XMPP server for compliance"}">
    <title>${title!"Check your XMPP server for compliance"}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta property="og:description" content="${description}">
    <meta property="og:locale" content="en_US">
    <meta charset="UTF-8">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.3.1/jquery.js"></script>
</head>

<body>
<nav id="nav_bar">
    <script type="application/javascript">
        function menu_toggle() {
            var list_menu = document.getElementById("nav_list_menu");
            var hamburger = document.getElementById("hamburger");
            if (hamburger.classList.contains("clicked")) {
                hamburger.classList.remove("clicked");
            } else {
                hamburger.classList.add("clicked");
            }
            if (list_menu.classList.contains("expanded")) {
                list_menu.classList.remove("expanded");
            } else {
                list_menu.classList.add("expanded");
            }
        }
    </script>
    <div id="brand" href="#">
        <h1 id="brand_name">${project_name}</h1>
    </div>
    <div id="hamburger" onclick="menu_toggle()">
        <span></span>
        <span></span>
        <span></span>
    </div>
    <div id="nav_list_menu" onclick="menu_toggle()">
        <a href="/add">
            Test your server
        </a>
        <a href="#">
            Servers
        </a>
        <a href="#">
            Tests
        </a>
        <a href="#">
            About
        </a>
    </div>
</nav>
<div id="content">
            <#nested>
</div>
<footer>
    <small>
        Copyright &copy; 2018 <a href="https://gultsch.de/">Daniel Gultsch</a>, <a href="https://rishiraj22.github.io">Rishi
        Raj</a>
    </small>
</footer>
</body>
</html>
</#macro>