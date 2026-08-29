<?php
declare(strict_types=1);

namespace App\Runtime;

use App\Kernel;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpKernel\KernelInterface;

final class WebspaceFrontController
{
    public static function run(string $projectDir): void
    {
        @ini_set('display_errors', '0');
        @ini_set('zend.exception_ignore_args', '1');

        $debug = false;
        try {
            $config = SharedHostingConfig::load($projectDir);
            $debug = $config['debug'];

            // Symfony's verbose debug exception pages are deliberately disabled on the public gateway,
            // because they can expose environment/server metadata. APP_DEBUG enables only safe logging below.
            $kernel = new Kernel($config['appEnv'], false);
            $request = WebspaceRequestFactory::fromGlobals();
            $response = $kernel->handle($request, KernelInterface::MAIN_REQUEST, true);
        } catch (\Throwable $error) {
            self::logSafely($debug, $error);
            $response = new JsonResponse([
                'error' => [
                    'code' => 'BOOTSTRAP_ERROR',
                    'message' => 'Gateway konnte nicht gestartet werden.',
                ],
            ], 500);
            $response->headers->set('X-Content-Type-Options', 'nosniff');
            $response->send();
            return;
        }

        $response->headers->set('X-Content-Type-Options', 'nosniff');
        $response->send();

        try {
            $kernel->terminate($request, $response);
        } catch (\Throwable $error) {
            self::logSafely($debug, $error);
        }
    }

    private static function logSafely(bool $debug, \Throwable $error): void
    {
        if (!$debug) {
            return;
        }
        $path = parse_url((string) ($_SERVER['REQUEST_URI'] ?? ''), PHP_URL_PATH) ?: 'unknown';
        error_log(sprintf('HealthAgent gateway error type=%s path=%s', $error::class, $path));
    }
}
