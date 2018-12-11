If you are using a reverse proxy or an HTTP Upload external script make sure you are setting the correct CORS headers:
```
modules:
  ...
  mod_http_upload:
    ...
    custom_headers:
      "Access-Control-Allow-Origin": "https://domain.tld"
      "Access-Control-Allow-Methods": "GET,HEAD,PUT,OPTIONS"
      "Access-Control-Allow-Headers": "Content-Type"
```
