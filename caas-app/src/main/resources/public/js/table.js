$(function () {
    function fixTable(container, div_header, div_first_col) {
        var thead = container.querySelector('thead');
        var ths = [].slice.call(thead.querySelectorAll('th'));
        var tbody = container.querySelector('tbody');
        var trs = [].slice.call(tbody.querySelectorAll('tr'));
        var firstColumnChildren = [].slice.call(div_first_col.children);

        document.querySelector('body').style.overflow = 'hidden'; // One time thing

        function relayout() {
            var screenWidth = window.innerWidth;
            var screenHeight = window.innerHeight;

            trs.forEach(function (tr, i) {
                var tds = [].slice.call(tr.querySelectorAll('td'));
                tds.forEach(function (td, j) {
                    td.style.display = '';
                    td.style["min-width"] = '';
                });
            });

            thead.style.display = '';
            thead.setAttribute('style','');

            var thStyles = ths.map(function (th) {
                var style = document.defaultView.getComputedStyle(th);
                var rect = th.getBoundingClientRect();
                return {
                    height: style.height,
                    width: style.width,
                    boundingHeight: rect.height,
                    boundingWidth: rect.width
                }
            });
            var theadStyle = document.defaultView.getComputedStyle(thead);
            var headerHeight = theadStyle.height;
            var headerWidth = theadStyle.width;
            var rowHeight = parseInt(document.defaultView.getComputedStyle(tbody.querySelector("tr")).height) - 22 + 'px';

            div_header.style.height = headerHeight;
            div_header.style.width = headerWidth;

            div_first_col.style.width = thStyles[0].boundingWidth;
            div_first_col.style.height = document.defaultView.getComputedStyle(tbody).height;

            firstColumnChildren.forEach(function (serverNameDiv) {
                serverNameDiv.style.height = rowHeight;
            });

            var headerBoundingHeight = div_header.getBoundingClientRect().height;
            var firstColumnBoundingWidth = div_first_col.getBoundingClientRect().width;
            var containerHeight = screenHeight - 80 - 16 - 30 - thStyles[0].boundingHeight + 'px';
            var containerWidth = screenWidth - firstColumnBoundingWidth + 'px';
            container.style["max-height"] = containerHeight;
            container.style["max-width"] = containerWidth;
            container.style.marginTop = headerBoundingHeight + 'px';
            div_first_col.style.marginTop = headerBoundingHeight + 'px';
            container.style.marginLeft = firstColumnBoundingWidth + 'px';

            var headerHeight = parseInt(thStyles[0].height) - 2 + 'px';
            thStyles.forEach(function (thStyle, i) {
                var headerDiv = document.createElement("div");
                headerDiv.className = "header_element";
                headerDiv.innerHTML = ths[i].innerHTML;
                headerDiv.style.width = thStyle.width;
                headerDiv.style.height = headerHeight;
                div_header.appendChild(headerDiv);
                if (i === 0) {
                    headerDiv.style.borderLeft = '0';
                }
                else if (i === thStyles.length - 1) {
                    headerDiv.style.borderRight = '0'
                }
            });
            trs.forEach(function (tr, i) {
                var tds = [].slice.call(tr.querySelectorAll('td'));
                tds.forEach(function (td, j) {
                    if (j === 0) {
                        td.style.display = 'none';
                    }
                    if (j > 1) {
                        td.style["min-width"] = thStyles[j].width;
                    }
                });
            });

            thead.style.display = 'none';
            div_header.style.top = thStyles[0].top;

        }

        relayout();

        div_header.style.visibility = 'visible';
        div_first_col.style.visibility = 'visible';

        //Add sticky behaviour to first column and header
        container.addEventListener('scroll', function () {
            div_header.style.transform = 'translate3d(' + (-this.scrollLeft) + 'px,0,0)';
            div_first_col.style.transform = "translate3d(0," + (-this.scrollTop) + "px,0)";
        });

        // Add hover to server name
        trs.forEach(function (tr, i) {
            tr.addEventListener('mouseover', function () {
                firstColumnChildren[i].classList.add("hover");
            }.bind(this));
            tr.addEventListener('mouseout', function () {
                firstColumnChildren[i].classList.remove("hover");
            }.bind(this));
        });

        //Add relayout feature
        var resizeTimeout;

        function resizeThrottler() {
            if (!resizeTimeout) {
                resizeTimeout = setTimeout(function () {
                    resizeTimeout = null;
                    relayout();
                }, 800);
            }
        }

        window.addEventListener('resize', resizeThrottler, false);

        return {
            relayout: relayout
        };
    }


    //For IE/Edge, skip fixing the table as it does not seem to work
    if (document.documentMode || /Edge/.test(navigator.userAgent)) {
        $("#results_table").after($("<p class='error_message'></p>").text("WARNING: Some features will not work in IE/Edge"));
        return;
    }

    var container = document.getElementById("results_table");
    var div_header = document.getElementById("div_header");
    var div_first_col = document.getElementById("div_first_col");

    var fixedTable = fixTable(container, div_header, div_first_col);

    //Make footer take the rest of the screen
    document.querySelector("body").style.overflow = "hidden";
    document.querySelector("footer").style.height = "100%";
});