<?php
declare(strict_types=1);

namespace App\Tests\Runtime;

use App\Kernel;
use PHPUnit\Framework\TestCase;

final class KernelProjectDirTest extends TestCase
{
    public function testProjectDirIsGatewayRootEvenWithoutComposerJson(): void
    {
        $kernel = new Kernel('prod', false);

        self::assertSame(
            realpath(dirname(__DIR__, 2)),
            realpath($kernel->getProjectDir()),
        );
        self::assertFileExists($kernel->getProjectDir().'/config/routes.yaml');
    }
}
