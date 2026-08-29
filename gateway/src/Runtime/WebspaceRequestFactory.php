<?php
declare(strict_types=1);

namespace App\Runtime;

use Symfony\Component\HttpFoundation\Request;

final class WebspaceRequestFactory
{
    public static function fromGlobals(): Request
    {
        $request = Request::createFromGlobals();
        $normalizedUri = self::normalizeUri($request->getRequestUri(), $_SERVER);

        if ($normalizedUri === $request->getRequestUri()) {
            return $request;
        }

        $server = $_SERVER;
        $server['REQUEST_URI'] = $normalizedUri;

        // After removing the hosting subdirectory, make Symfony treat the
        // rewritten front controller as if it lived at the application root.
        // This avoids a second base-path calculation from CGI/FastCGI values.
        $server['SCRIPT_NAME'] = '/index.php';
        $server['PHP_SELF'] = '/index.php';
        $server['ORIG_SCRIPT_NAME'] = '/index.php';

        return new Request(
            $request->query->all(),
            $request->request->all(),
            [],
            $request->cookies->all(),
            $request->files->all(),
            $server,
            $request->getContent(),
        );
    }

    /**
     * Shared hosts can pass REQUEST_URI including the physical deployment
     * directory even though Apache has already rewritten the request to the
     * gateway front controller. Symfony then sees e.g. /gateway/health while
     * the application route is /health. Strip only a prefix that can be
     * derived from the actual front-controller location.
     *
     * @param array<string,mixed> $server
     */
    public static function normalizeUri(string $requestUri, array $server): string
    {
        $path = parse_url($requestUri, PHP_URL_PATH);
        if (!is_string($path) || $path === '') {
            $path = '/';
        }

        $prefixes = [];
        self::collectScriptPrefixes($prefixes, $server['SCRIPT_NAME'] ?? null);
        self::collectScriptPrefixes($prefixes, $server['PHP_SELF'] ?? null);
        self::collectScriptPrefixes($prefixes, $server['ORIG_SCRIPT_NAME'] ?? null);

        $documentRoot = self::normalizeFsPath($server['DOCUMENT_ROOT'] ?? null);
        $scriptFilename = self::normalizeFsPath($server['SCRIPT_FILENAME'] ?? null);
        if ($documentRoot !== null && $scriptFilename !== null) {
            $root = rtrim($documentRoot, '/');
            if ($root !== '' && str_starts_with($scriptFilename, $root.'/')) {
                self::collectScriptPrefixes($prefixes, substr($scriptFilename, strlen($root)));
            }
        }

        $prefixes = array_values(array_unique(array_filter(
            $prefixes,
            static fn (string $prefix): bool => $prefix !== '' && $prefix !== '/',
        )));
        usort($prefixes, static fn (string $a, string $b): int => strlen($b) <=> strlen($a));

        foreach ($prefixes as $prefix) {
            if ($path === $prefix) {
                $path = '/';
                break;
            }
            if (str_starts_with($path, $prefix.'/')) {
                $path = substr($path, strlen($prefix));
                if ($path === '') {
                    $path = '/';
                }
                break;
            }
        }

        if (!str_starts_with($path, '/')) {
            $path = '/'.$path;
        }

        $query = parse_url($requestUri, PHP_URL_QUERY);
        return $path.(is_string($query) && $query !== '' ? '?'.$query : '');
    }

    /** @param list<string> $prefixes */
    private static function collectScriptPrefixes(array &$prefixes, mixed $value): void
    {
        if (!is_string($value) || $value === '') {
            return;
        }

        $value = str_replace('\\', '/', $value);
        $phpPosition = stripos($value, '.php');
        if ($phpPosition === false) {
            return;
        }

        $scriptPath = substr($value, 0, $phpPosition + 4);
        if (!str_starts_with($scriptPath, '/')) {
            $scriptPath = '/'.$scriptPath;
        }

        $scriptPath = rtrim($scriptPath, '/');
        $directory = str_replace('\\', '/', dirname($scriptPath));
        $directory = $directory === '.' ? '' : rtrim($directory, '/');

        $prefixes[] = $scriptPath;
        if ($directory !== '') {
            $prefixes[] = $directory;
        }
    }

    private static function normalizeFsPath(mixed $value): ?string
    {
        if (!is_string($value) || trim($value) === '') {
            return null;
        }

        return str_replace('\\', '/', rtrim(trim($value), '/\\'));
    }
}
