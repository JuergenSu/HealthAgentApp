<?php
declare(strict_types=1);

namespace App;

use Symfony\Bundle\FrameworkBundle\Kernel\MicroKernelTrait;
use Symfony\Component\HttpKernel\Kernel as BaseKernel;

final class Kernel extends BaseKernel
{
    use MicroKernelTrait;

    public function getProjectDir(): string
    {
        // The deployable webspace artifact intentionally contains no composer.json.
        // BaseKernel would otherwise fall back to the directory containing this
        // Kernel class (`src/`) and Symfony would miss `config/routes.yaml`.
        return dirname(__DIR__);
    }

    public function getCacheDir(): string
    {
        return $this->runtimeDir().'/cache/'.$this->environment;
    }

    public function getLogDir(): string
    {
        return $this->runtimeDir().'/log';
    }

    private function runtimeDir(): string
    {
        $configured = trim((string) getenv('APP_RUNTIME_DIR'));
        if ($configured !== '') {
            return rtrim($configured, '/\\');
        }

        // Shared-hosting /tmp directories survive FTP deployments. Symfony's prod
        // cache must therefore not be reused across different uploaded artifacts.
        // The packaging step writes a content-derived deployment id so each changed
        // release receives a fresh cache namespace without requiring shell access.
        $namespace = substr(hash('sha256', $this->getProjectDir().'|'.$this->deploymentId()), 0, 16);
        return rtrim(sys_get_temp_dir(), '/\\').DIRECTORY_SEPARATOR.'health-agent-gateway-'.$namespace;
    }

    private function deploymentId(): string
    {
        $path = $this->getProjectDir().'/config/deployment.id';
        if (!is_file($path)) {
            return 'development';
        }

        $value = trim((string) file_get_contents($path));
        return preg_match('/^[a-f0-9]{64}$/', $value) === 1 ? $value : 'development';
    }
}
