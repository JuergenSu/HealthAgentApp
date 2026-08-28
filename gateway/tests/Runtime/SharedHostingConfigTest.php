<?php
declare(strict_types=1);

namespace App\Tests\Runtime;

use App\Runtime\SharedHostingConfig;
use PHPUnit\Framework\TestCase;

final class SharedHostingConfigTest extends TestCase
{
    private const KEYS = [
        'APP_ENV', 'APP_DEBUG', 'APP_SECRET', 'APP_RUNTIME_DIR',
        'OPENAI_API_KEY', 'OPENAI_MODEL', 'COACH_CORS_ORIGINS',
    ];

    private array $saved = [];
    private string $dir;

    protected function setUp(): void
    {
        foreach (self::KEYS as $key) {
            $this->saved[$key] = [
                'process' => getenv($key),
                'envExists' => array_key_exists($key, $_ENV),
                'env' => $_ENV[$key] ?? null,
                'serverExists' => array_key_exists($key, $_SERVER),
                'server' => $_SERVER[$key] ?? null,
            ];
            putenv($key);
            unset($_ENV[$key], $_SERVER[$key]);
        }

        $this->dir = sys_get_temp_dir().'/health-agent-config-'.bin2hex(random_bytes(5));
        mkdir($this->dir.'/config', 0777, true);
    }

    protected function tearDown(): void
    {
        @unlink($this->dir.'/.env');
        @unlink($this->dir.'/config/config.local.php');
        @rmdir($this->dir.'/config');
        @rmdir($this->dir);

        foreach ($this->saved as $key => $saved) {
            if ($saved['process'] === false) {
                putenv($key);
            } else {
                putenv($key.'='.$saved['process']);
            }
            if ($saved['envExists']) {
                $_ENV[$key] = $saved['env'];
            } else {
                unset($_ENV[$key]);
            }
            if ($saved['serverExists']) {
                $_SERVER[$key] = $saved['server'];
            } else {
                unset($_SERVER[$key]);
            }
        }
    }

    public function testLocalConfigOverridesDotEnvAndDefaultsAreApplied(): void
    {
        file_put_contents($this->dir.'/.env', "OPENAI_MODEL=env-model\nCOACH_CORS_ORIGINS=https://env.example\n");
        file_put_contents($this->dir.'/config/config.local.php', <<<'PHP'
<?php
return [
    'OPENAI_MODEL' => 'local-model',
    'COACH_CORS_ORIGINS' => ['https://one.example', 'https://two.example'],
    'APP_DEBUG' => true,
];
PHP);

        $config = SharedHostingConfig::load($this->dir);

        self::assertSame('prod', $config['appEnv']);
        self::assertTrue($config['debug']);
        self::assertSame('local-model', getenv('OPENAI_MODEL'));
        self::assertSame('https://one.example,https://two.example', getenv('COACH_CORS_ORIGINS'));
        self::assertSame('', getenv('OPENAI_API_KEY'));
        self::assertNotSame('', getenv('APP_SECRET'));
    }

    public function testHostingEnvironmentHasHighestPriority(): void
    {
        file_put_contents($this->dir.'/.env', "OPENAI_MODEL=env-model\n");
        file_put_contents($this->dir.'/config/config.local.php', "<?php return ['OPENAI_MODEL' => 'local-model'];");
        putenv('OPENAI_MODEL=hosting-model');
        $_ENV['OPENAI_MODEL'] = 'hosting-model';

        SharedHostingConfig::load($this->dir);

        self::assertSame('hosting-model', getenv('OPENAI_MODEL'));
    }

    public function testUnknownLocalOptionFailsClosedWithoutEchoingItsValue(): void
    {
        file_put_contents($this->dir.'/config/config.local.php', "<?php return ['UNKNOWN_SECRET_OPTION' => 'do-not-print-me'];");

        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Unbekannte lokale Gateway-Konfigurationsoption.');

        SharedHostingConfig::load($this->dir);
    }
}
