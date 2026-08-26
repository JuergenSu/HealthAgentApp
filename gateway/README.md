# PHP Coach Gateway

Stateless Symfony gateway between the Android app and OpenAI. The gateway does not persist health data and has no health database.

## Requirements

- PHP 8.3+
- Composer 2

## Local setup

```bash
cd gateway
composer install
cp .env.example .env
```

Set `APP_SECRET`, `OPENAI_API_KEY` and `OPENAI_MODEL` through environment/secret configuration. The OpenAI key must never be committed or shipped with Android.

For local development you can run Symfony CLI (`symfony server:start`) or PHP's development server:

```bash
APP_ENV=dev APP_DEBUG=1 APP_SECRET=local OPENAI_API_KEY=dummy OPENAI_MODEL=dummy php -S 127.0.0.1:8080 -t public
```

`GET /health` returns `{"status":"ok"}`.

`POST /api/v1/coach` accepts JSON containing at least a non-empty `message`. Until issue #28 integrates the Responses API, valid coach requests deliberately return the stable `UPSTREAM_UNAVAILABLE` response. This keeps #27 independently testable without a live OpenAI dependency.

## Tests

```bash
composer test
```

Tests require no live OpenAI API key. GitHub Actions runs Composer validation, a clean dependency installation and PHPUnit for changes under `gateway/`.

## Security boundary

The service is stateless with respect to health/fitness data. Android supplies an allow-listed request context. No request payload is persisted by the gateway. OpenAI access is isolated behind `App\\OpenAI\\OpenAiClient`; controllers do not depend on a vendor SDK.
