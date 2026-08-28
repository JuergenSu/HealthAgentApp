<?php
declare(strict_types=1);

namespace App\Runtime;

use Symfony\Component\Dotenv\Dotenv;

final class SharedHostingConfig
{
    private const KEYS = [
        'APP_ENV',
        'APP_DEBUG',
        'APP_SECRET',
        'APP_RUNTIME_DIR',
        'OPENAI_API_KEY',
        'OPENAI_MODEL',
        'COACH_CORS_ORIGINS',
    ];

    private const DEFAULTS = [
        'APP_ENV' => 'prod',
        'APP_DEBUG' => '0',
        'APP_RUNTIME_DIR' => '',
        'OPENAI_API_KEY' => '',
        'OPENAI_MODEL' => 'gpt-5.6',
        'COACH_CORS_ORIGINS' => '',
    ];

    /** @return array{appEnv:string,debug:bool} */
    public static function load(string $projectDir): array
    {
        $external = [];
        foreach (self::KEYS as $key) {
            $value = self::rawValue($key);
            if ($value !== null) {
                $external[$key] = $value;
            }
        }

        $resolved = self::DEFAULTS;
        $envFile = rtrim($projectDir, '/\\').'/.env';
        if (is_file($envFile)) {
            $contents = file_get_contents($envFile);
            if ($contents === false) {
                throw new \RuntimeException('Die lokale .env-Datei konnte nicht gelesen werden.');
            }
            $parsed = (new Dotenv())->parse($contents, $envFile);
            foreach (self::KEYS as $key) {
                if (array_key_exists($key, $parsed)) {
                    $resolved[$key] = self::normalize($key, $parsed[$key]);
                }
            }
        }

        $localFile = rtrim($projectDir, '/\\').'/config/config.local.php';
        if (is_file($localFile)) {
            $local = require $localFile;
            if (!is_array($local)) {
                throw new \RuntimeException('config.local.php muss ein Array zurückgeben.');
            }
            foreach ($local as $key => $value) {
                if (!is_string($key) || !in_array($key, self::KEYS, true)) {
                    throw new \InvalidArgumentException('Unbekannte lokale Gateway-Konfigurationsoption.');
                }
                $resolved[$key] = self::normalize($key, $value);
            }
        }

        // Hosting-panel / process variables have the highest priority.
        $resolved = array_replace($resolved, $external);
        if (($resolved['APP_SECRET'] ?? '') === '') {
            $resolved['APP_SECRET'] = hash('sha256', rtrim($projectDir, '/\\').'|health-agent-stateless-gateway');
        }

        foreach ($resolved as $key => $value) {
            self::setRuntimeValue($key, (string) $value);
        }

        return [
            'appEnv' => self::value('APP_ENV') ?: 'prod',
            'debug' => self::toBool(self::value('APP_DEBUG')),
        ];
    }

    public static function value(string $key): ?string
    {
        return self::rawValue($key);
    }

    private static function rawValue(string $key): ?string
    {
        $value = getenv($key);
        if ($value !== false) {
            return (string) $value;
        }
        if (array_key_exists($key, $_ENV)) {
            return (string) $_ENV[$key];
        }
        if (array_key_exists($key, $_SERVER) && is_scalar($_SERVER[$key])) {
            return (string) $_SERVER[$key];
        }
        return null;
    }

    private static function normalize(string $key, mixed $value): string
    {
        if ($key === 'COACH_CORS_ORIGINS' && is_array($value)) {
            $origins = [];
            foreach ($value as $origin) {
                if (!is_scalar($origin)) {
                    throw new \InvalidArgumentException('CORS-Origins müssen Strings sein.');
                }
                $origins[] = trim((string) $origin);
            }
            return implode(',', array_filter($origins, static fn (string $origin): bool => $origin !== ''));
        }
        if (is_bool($value)) {
            return $value ? '1' : '0';
        }
        if ($value === null) {
            return '';
        }
        if (!is_scalar($value)) {
            throw new \InvalidArgumentException('Gateway-Konfigurationswerte müssen skalar sein.');
        }
        return trim((string) $value);
    }

    private static function setRuntimeValue(string $key, string $value): void
    {
        putenv($key.'='.$value);
        $_ENV[$key] = $value;
    }

    private static function toBool(?string $value): bool
    {
        return in_array(strtolower(trim((string) $value)), ['1', 'true', 'yes', 'on'], true);
    }
}
