<#macro page>
<html>
<head>
    <style>
        body, #content {
            color: rgba(0, 0, 0, 0.87);
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
            font-size: 13pt;
            background-color: #fafafa;
        }

        #content {
            margin-top: 100px;
            text-align: center;
        }

        input {
            font-size: 0.9em;
            background: none;
            text-decoration: none;
            outline: none !important;
            border: none;
            border-bottom: solid 2px #43A047;
        }

        a {
            color: #43A047;
        }

        .button, button {
            font-size: 0.95em;
            background: #43A047;
            color: white;
            box-shadow: 0 3px 6px 0 rgba(0, 0, 0, 0.2);
            border: none;
            cursor: pointer;
            text-align: center;
            border-radius: 8px;
            margin-top: 10px;
            padding: 10px;
        }

        .button:hover, button:hover {
            background: #FFFFFF;
            color: #43A047;
            box-shadow: 0 3px 6px 0 rgba(0, 0, 0, 0.2);
            -webkit-transition: all 0.3s;
            -transition: all 0.3s;
            -ms-transition: all 0.3s;
            -o-transition: all 0.3s;
            transition: all 0.3s;
        }

        .card {
            background: #ffffff;
            box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2);
        }

        .chip {
            display: inline-flex;
            padding: 10px;
            background: #ffffff;
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
        #nav_bar {
            position: absolute;
            top: 0px;
            right: 0px;
            background: #43A047;
            left: 0px;
            z-index: 200;
            height: 80px;
        }

        #nav_bar * {
            color: #FFFFFF;
        }

        #brand {
            float: left;
            margin: 0px;
            margin-left: 20px;
            height: 80px;
        }

        #nav_list_menu {
            float: right;
            margin: 0px;
            padding-top: 30px;
            padding-bottom: 30px;
            height: 20px;
        }

        #nav_list_menu a {
            margin: 0px;
            padding: 0px;
            font-size: 20px;
            padding-right: 15px;
            text-decoration: none;
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
            display: none;
            height: 110px;
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
                margin: 0px;
                padding: 0px;
                background-color: #43A047;
                color: #656565;
                width: 100%;
                -webkit-transform: translate(1000px, 0px);
                transform: translate(1000px, 0px);
                -webkit-transition: -webkit-transform 300ms;
                transition: -webkit-transform 300ms;
                transition: transform 300ms;
                transition: transform 300ms, -webkit-transform 300ms;
                height: auto;
            }

            #nav_list_menu.expanded {
                display: block;
                -webkit-transform: translate(0px, 0px);
                transform: translate(0px, 0px);
            }

            #nav_list_menu a {
                display: block;
                text-align: center;
                padding: 10px;
                width: 100%;
                -webkit-transition: height 300ms;
                transition: height 300ms;
                -webkit-transition: all 300ms ease-in;
                transition: all 300ms ease-in;
            }

            #nav_list_menu.expanded a:hover {
                background-color: #fafafa;
                color: #000000;
                -webkit-transition: all 300ms ease-in-out;
                transition: all 300ms ease-in-out;
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
                background-color: #ffffff;
                height: 10px;
                width: 55px;
            }
        }
    </style>
    <meta property="og:title" content="${title!"Check your XMPP server for compliance"}">
    <title>${title!"Check your XMPP server for compliance"}</title>
    <meta serverName="viewport" content="user-scalable=no,width=device-width, initial-scale=1.0">
    <meta property="og:description" content="${description}">
    <meta property="og:locale" content="en_US">
    <meta charset="UTF-8">
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
        <h1 id="brand_name">CaaS</h1>
    </div>
    <div id="hamburger" onclick="menu_toggle()">
        <span></span>
        <span></span>
        <span></span>
    </div>
    <div id="nav_list_menu" onclick="menu_toggle()">
        <a href="#">
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