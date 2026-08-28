<?php
declare(strict_types=1);

namespace App;

use Symfony\Bundle\FrameworkBundle\Kernel\MicroKernelTrait;
use Symfony\Component\HttpKernel\Kernel as BaseKernel;

final class Kernel extends BaseKernel
{
    use MicroKernelTrait;

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

        $namespace = substr(hash('sha256', $this->getProjectDir()), 0, 12);
        return rtrim(sys_get_temp_dir(), '/\\').DIRECTORY_SEPARATOR.'health-agent-gateway-'.$namespace;
    }
}
