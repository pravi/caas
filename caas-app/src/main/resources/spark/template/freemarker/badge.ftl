<#assign colors= [ "#e53935", "#FF7043", "#FBC02D", "#C0CA33", "#7CB342", "#43a047"]>
<#assign id= pass / total * 5>
    <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
         width="268" height="20">
        <linearGradient id="b" x2="0" y2="100%">
            <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
            <stop offset="1" stop-opacity=".1"/>
        </linearGradient>
        <clipPath id="a">
            <rect width="268" height="20" rx="3" fill="#fff"/>
        </clipPath>
        <a xlink:href="${resultLink}">
            <g clip-path="url(#a)">
                <path fill="#555" d="M0 0h185v20H0z"/>
                <path fill="${colors[id]}" d="M185 0h83v20H185z"/>
                <path fill="url(#b)" d="M0 0h268v20H0z"/>
            </g>
        </a>

        <g fill="#fff" text-anchor="middle" font-family="DejaVu Sans,Verdana,Geneva,sans-serif" font-size="110">
            <a xlink:href="${resultLink}">
                <text x="935" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)">XMPP
                    Specifications compliance
                </text>
                <text x="935" y="140" transform="scale(.1)">XMPP Specifications compliance
                </text>
            </a>
            <a xlink:href="${resultLink}">
                <text x="2255" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)">
                ${((100*pass)/total)?int}% (${pass}/${total})
                </text>
                <text x="2255" y="140" transform="scale(.1)">
                ${((100*pass)/total)?int}% (${pass}/${total})
                </text>
            </a>
        </g>
    </svg>