<?php
declare(strict_types=1);

namespace App\Tests\Runtime;

use App\Runtime\WebspaceRequestFactory;
use PHPUnit\Framework\TestCase;

final class WebspaceRequestFactoryTest extends TestCase
{
    public function testStripsDeploymentDirectoryFromCleanUrl(): void
    {
        self::assertSame('/health', WebspaceRequestFactory::normalizeUri('/gateway/health', [
            'SCRIPT_NAME' => '/gateway/index.php',
        ]));
    }

    public function testStripsDeploymentDirectoryAndExplicitFrontController(): void
    {
        self::assertSame('/health', WebspaceRequestFactory::normalizeUri('/gateway/index.php/health', [
            'SCRIPT_NAME' => '/gateway/index.php',
        ]));
    }

    public function testDerivesDeploymentDirectoryFromPhysicalScriptWhenCgiScriptNameIsWrong(): void
    {
        self::assertSame('/api/v1/coach?trace=1', WebspaceRequestFactory::normalizeUri('/gateway/api/v1/coach?trace=1', [
            'SCRIPT_NAME' => '/index.php',
            'SCRIPT_FILENAME' => '/home/www/gateway/index.php',
            'DOCUMENT_ROOT' => '/home/www',
        ]));
    }

    public function testRootDeploymentIsUnchanged(): void
    {
        self::assertSame('/health', WebspaceRequestFactory::normalizeUri('/health', [
            'SCRIPT_NAME' => '/index.php',
            'SCRIPT_FILENAME' => '/home/www/index.php',
            'DOCUMENT_ROOT' => '/home/www',
        ]));
    }

    public function testDirectoryRootNormalizesToApplicationRoot(): void
    {
        self::assertSame('/', WebspaceRequestFactory::normalizeUri('/gateway/', [
            'SCRIPT_NAME' => '/gateway/index.php',
        ]));
    }
}
