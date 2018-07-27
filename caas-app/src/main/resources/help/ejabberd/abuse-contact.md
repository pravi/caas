* In mod\_disco, add server\_info with email for reporting abuse:
```
modules:
    ...
    mod_disco:
        server_info:
        -
            modules: all
            name: "abuse-addresses"
            urls: ["mailto:abusecontact@yourserver.com"]
```
