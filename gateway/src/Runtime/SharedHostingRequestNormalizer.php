<?php
declare(strict_types=1);

namespace App\Runtime;

final class SharedHostingRequestNormalizer
{
    /**
     * Normalize shared-hosting request metadata before Symfony creates the Request.
     *
     * Some PHP/Apache hosting stacks expose the deployment directory as part of
     * REQUEST_URI/SCRIPT_NAME without providing PATH_INFO. Symfony can then see
     * `/gateway/health` instead of the application route `/health`.
     *
     * @param array<string, mixed> $server
     * @return array<string, mixed>
     */
    public static function normalize(array $server): array
    {
        $requestUri = (string) ($server['REQUEST_URI'] ?? '/');
        $path = parse_url($requestUri, PHP_URL_PATH);
        if (!is_string($path) || $path === '') {
            return $server;
        }

        $logicalPath = $path;
        $basePath = self::deploymentBasePath($server);
        if ($basePath !== '' && ($logicalPath === $basePath || str_starts_with($logicalPath, $basePath.'/'))) {
            $logicalPath = substr($logicalPath, strlen($basePath));
            if ($logicalPath === '') {
                $logicalPath = '/';
            }
        }

        if ($logicalPath === '/index.php') {
            $logicalPath = '/';
        } elseif (str_starts_with($logicalPath, '/index.php/')) {
            $logicalPath = substr($logicalPath, strlen('/index.php'));
        }

        if ($logicalPath === '' || $logicalPath[0] !== '/') {
            $logicalPath = '/'.$logicalPath;
        }

        $query = parse_url($requestUri, PHP_URL_QUERY);
        $server['REQUEST_URI'] = $logicalPath.($query !== null && $query !== '' ? '?'.$query : '');

        // Once REQUEST_URI is application-relative, keep the front-controller
        // metadata application-relative as well so HttpFoundation cannot infer
        // the physical deployment folder as part of the route path.
        $server['SCRIPT_NAME'] = '/index.php';
        $server['PHP_SELF'] = '/index.php';
        unset($server['PATH_INFO'], $server['ORIG_PATH_INFO']);

        return $server;
    }

    /** @param array<string, mixed> $server */
    private static function deploymentBasePath(array $server): string
    {
        $documentRoot = self::filesystemPath((string) ($server['DOCUMENT_ROOT'] ?? ''));
        $scriptFilename = self::filesystemPath((string) ($server['SCRIPT_FILENAME'] ?? ''));
        if ($documentRoot === '' || $scriptFilename === '') {
            return '';
        }

        $documentRoot = rtrim($documentRoot, '/');
        $scriptDir = rtrim(dirname($scriptFilename), '/');
        if ($documentRoot === '' || $scriptDir === $documentRoot) {
            return '';
        }

        $prefix = $documentRoot.'/';
        if (!str_starts_with($scriptDir.'/', $prefix)) {
            return '';
        }

        $relative = trim(substr($scriptDir, strlen($documentRoot)), '/');
        return $relative === '' ? '' : '/'.$relative;
    }

    private static function filesystemPath(string $path): string
    {
        return str_replace('\\', '/', trim($path));
    }
}
