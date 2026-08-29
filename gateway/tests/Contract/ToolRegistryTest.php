<?php
declare(strict_types=1);

namespace App\Tests\Contract;

use App\Agent\ToolRegistry;
use PHPUnit\Framework\TestCase;

final class ToolRegistryTest extends TestCase
{
    public function testArgumentlessToolsEncodePropertiesAsJsonObjects(): void
    {
        $definitions = ToolRegistry::definitions();

        $todayStatus = $this->definition($definitions, 'get_today_status');
        self::assertInstanceOf(\stdClass::class, $todayStatus['parameters']['properties']);
        self::assertSame(
            '{}',
            json_encode($todayStatus['parameters']['properties'], JSON_THROW_ON_ERROR),
        );

        $activeGoal = $this->definition($definitions, 'get_active_goal');
        self::assertInstanceOf(\stdClass::class, $activeGoal['parameters']['properties']);
    }

    public function testArgumentlessToolStillRejectsUnexpectedArguments(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('unknown tool argument');

        ToolRegistry::validate('get_today_status', ['unexpected' => true]);
    }

    private function definition(array $definitions, string $name): array
    {
        foreach ($definitions as $definition) {
            if (($definition['name'] ?? null) === $name) {
                return $definition;
            }
        }

        self::fail(sprintf('Tool %s not found.', $name));
    }
}
