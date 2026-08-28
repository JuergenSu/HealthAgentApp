# PHP Coach Gateway

Stateless PHP/Symfony gateway between the Android app and OpenAI. The gateway persists no health data and has no database. It can be deployed to ordinary Apache/PHP shared hosting without SSH, Composer or a framework server on the target host.

## Requirements

- PHP 8.2+
- HTTPS for production use
- Apache is recommended for clean URLs; without URL rewriting the explicit `index.php/...` fallback works

Composer is required only for development or for building the deployment ZIP. It is **not** required on the target webspace.

## Fastest shared-hosting deployment

1. Download the `health-agent-php-gateway` artifact from the **PHP Gateway CI** GitHub Actions run and unzip `health-agent-php-gateway.zip`.
2. Upload the contained `coach-gateway/` directory by FTP/SFTP or the hosting file manager.
3. Configure `OPENAI_API_KEY` and `OPENAI_MODEL` either in the hosting environment, in a local `.env`, or by copying `config/config.local.php.example` to `config/config.local.php` and editing it.
4. Preferred: configure the domain/subdomain document root to `coach-gateway/public`.
5. Call `/health`. Expected response: `{"status":"ok"}`.
6. Configure the Android `COACH_GATEWAY_URL` to the base URL of the gateway.

If the host cannot change the document root, the complete `coach-gateway/` directory may itself be placed below the web root. The supplied root `.htaccess` protects internal directories and routes requests through the front controller. This mode requires Apache `mod_rewrite`.

## Configuration precedence

Highest priority wins:

1. Hosting/process environment variables
2. `config/config.local.php`
3. `.env`
4. safe application defaults

Supported settings:

- `OPENAI_API_KEY` – server-side OpenAI key; never ship it with Android
- `OPENAI_MODEL` – model name, default `gpt-5.6`
- `COACH_CORS_ORIGINS` – optional comma-separated browser origins or `*`; native Android does not need CORS
- `APP_DEBUG` – safe diagnostic mode (`0`/`1`); logs only the exception class and request path for bootstrap failures, never configuration values or the API key
- `APP_RUNTIME_DIR` – optional writable cache/log directory; by default the PHP system temp directory is used
- `APP_ENV` – normally `prod`

`config/config.local.php` is ignored by Git and deliberately excluded from deployment builds when it exists locally, so a developer secret cannot accidentally enter the ZIP. The artifact contains only `config.local.php.example`.

Example local configuration:

```php
<?php
return [
    'OPENAI_API_KEY' => 'replace-me',
    'OPENAI_MODEL' => 'gpt-5.6',
    'COACH_CORS_ORIGINS' => ['https://app.example.org'],
    'APP_DEBUG' => false,
];
```

## Endpoints

Clean URLs with Apache rewriting:

```text
GET  /health
POST /api/v1/coach
```

Fallback when URL rewriting is unavailable and the document root points to `public/`:

```text
GET  /index.php/health
POST /index.php/api/v1/coach
```

The Android base URL can therefore also be set to `https://example.org/coach/index.php`; the client will append `/api/v1/coach` and use the fallback route automatically.

Example request:

```bash
curl -X POST 'https://example.org/api/v1/coach' \
  -H 'Content-Type: application/json' \
  --data '{"message":"Was steht heute im Training an?","context":{}}'
```

The gateway keeps the existing structured `FINAL` / `TOOL_CALL` contract, validates the context/tool allowlist and forwards no direct Room or Health Connect access to PHP/OpenAI.

## Local development

```bash
cd gateway
composer install
cp .env.example .env
# edit .env
php -S 127.0.0.1:8080 -t public public/index.php
```

Then call `http://127.0.0.1:8080/health`.

Run tests:

```bash
composer test
```

Build the same deployable ZIP locally:

```bash
composer install --no-dev --classmap-authoritative
composer build:webspace
```

The result is `build/health-agent-php-gateway.zip` and contains `vendor/`, so no Composer invocation is needed on the webspace.

## Troubleshooting shared hosting

- **404 on `/health`**: try `/index.php/health`. If that works, URL rewriting is missing or `.htaccess` overrides are disabled.
- **500 immediately after upload**: verify PHP 8.2+, `vendor/` was uploaded completely, and the host allows the directives in `.htaccess`. Also check the provider's PHP error log.
- **Wrong document root**: preferred root is the uploaded `public/` directory. If that is impossible, use the root deployment mode with Apache rewriting.
- **OpenAI request returns 503**: verify `OPENAI_API_KEY` and `OPENAI_MODEL`. `/health` intentionally works without a valid OpenAI key so hosting can be diagnosed separately.
- **`.env` is ignored by the hosting stack**: use `config/config.local.php` or the provider's environment-variable UI instead.
- **Permissions/cache errors**: by default runtime files use PHP's writable system temp directory; set `APP_RUNTIME_DIR` if the provider requires a custom location.
- **CORS in a browser**: configure the exact origin in `COACH_CORS_ORIGINS`; leave it empty for native Android-only use.

## Security boundary

The API key remains server-side. It is not returned by `/health`, validation errors or bootstrap errors and is never deliberately written to logs. The service is stateless with respect to health/fitness data. Android supplies only an allow-listed request context. OpenAI access remains isolated behind `App\OpenAI\OpenAiClient`; controllers do not depend directly on the vendor SDK.
