<?php
declare(strict_types=1);

namespace App\Tests\Runtime;

use App\Runtime\SharedHostingRequestNormalizer;
use PHPUnit\Framework\TestCase;

final class SharedHostingRequestNormalizerTest extends TestCase
{
    public function testHostEuropeSubdirectoryHealthRouteIsApplicationRelative(): void
    {
        $server = SharedHostingRequestNormalizer::normalize([
            'REQUEST_URI' => '/gateway/health',
            'SCRIPT_NAME' => '/gateway/index.php',
            'SCRIPT_FILENAME' => '/home/customer/www/example.org/gateway/index.php',
            'PHP_SELF' => '/gateway/index.php',
            'PATH_INFO' => null,
            'DOCUMENT_ROOT' => '/home/customer/www/example.org/',
        ]);

        self::assertSame('/health', $server['REQUEST_URI']);
        self::assertSame('/index.php', $server['SCRIPT_NAME']);
        self::assertSame('/index.php', $server['PHP_SELF']);
        self::assertArrayNotHasKey('PATH_INFO', $server);
    }

    public function testHostEuropeExplicitFrontControllerFallbackIsNormalized(): void
    {
        $server = SharedHostingRequestNormalizer::normalize([
            'REQUEST_URI' => '/gateway/index.php/health?probe=1',
            'SCRIPT_NAME' => '/gateway/index.php',
            'SCRIPT_FILENAME' => '/home/customer/www/example.org/gateway/index.php',
            'PHP_SELF' => '/gateway/index.php',
            'DOCUMENT_ROOT' => '/home/customer/www/example.org/',
        ]);

        self::assertSame('/health?probe=1', $server['REQUEST_URI']);
    }

    public function testCoachEndpointInSubdirectoryIsNormalized(): void
    {
        $server = SharedHostingRequestNormalizer::normalize([
            'REQUEST_URI' => '/gateway/api/v1/coach',
            'SCRIPT_NAME' => '/gateway/index.php',
            'SCRIPT_FILENAME' => '/home/customer/www/example.org/gateway/index.php',
            'PHP_SELF' => '/gateway/index.php',
            'DOCUMENT_ROOT' => '/home/customer/www/example.org/',
        ]);

        self::assertSame('/api/v1/coach', $server['REQUEST_URI']);
    }

    public function testDomainRootDeploymentRemainsUnchanged(): void
    {
        $server = SharedHostingRequestNormalizer::normalize([
            'REQUEST_URI' => '/health',
            'SCRIPT_NAME' => '/index.php',
            'SCRIPT_FILENAME' => '/home/customer/www/example.org/index.php',
            'PHP_SELF' => '/index.php',
            'DOCUMENT_ROOT' => '/home/customer/www/example.org/',
        ]);

        self::assertSame('/health', $server['REQUEST_URI']);
    }

    public function testPublicDocumentRootDeploymentRemainsUnchanged(): void
    {
        $server = SharedHostingRequestNormalizer::normalize([
            'REQUEST_URI' => '/api/v1/coach',
            'SCRIPT_NAME' => '/index.php',
            'SCRIPT_FILENAME' => '/srv/coach-gateway/public/index.php',
            'PHP_SELF' => '/index.php',
            'DOCUMENT_ROOT' => '/srv/coach-gateway/public',
        ]);

        self::assertSame('/api/v1/coach', $server['REQUEST_URI']);
    }
}
