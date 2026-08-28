<?php
declare(strict_types=1);

namespace App\Tests\Runtime;

use App\Runtime\CorsSubscriber;
use PHPUnit\Framework\TestCase;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpKernel\Event\RequestEvent;
use Symfony\Component\HttpKernel\Event\ResponseEvent;
use Symfony\Component\HttpKernel\HttpKernelInterface;

final class CorsSubscriberTest extends TestCase
{
    private mixed $saved;

    protected function setUp(): void
    {
        $this->saved = getenv('COACH_CORS_ORIGINS');
    }

    protected function tearDown(): void
    {
        if ($this->saved === false) {
            putenv('COACH_CORS_ORIGINS');
            unset($_ENV['COACH_CORS_ORIGINS']);
        } else {
            putenv('COACH_CORS_ORIGINS='.$this->saved);
            $_ENV['COACH_CORS_ORIGINS'] = (string) $this->saved;
        }
    }

    public function testAllowedPreflightIsHandled(): void
    {
        putenv('COACH_CORS_ORIGINS=https://app.example');
        $_ENV['COACH_CORS_ORIGINS'] = 'https://app.example';
        $request = Request::create('/api/v1/coach', 'OPTIONS', server: ['HTTP_ORIGIN' => 'https://app.example']);
        $event = new RequestEvent($this->createMock(HttpKernelInterface::class), $request, HttpKernelInterface::MAIN_REQUEST);

        (new CorsSubscriber())->onRequest($event);

        self::assertTrue($event->hasResponse());
        self::assertSame(204, $event->getResponse()->getStatusCode());
        self::assertSame('https://app.example', $event->getResponse()->headers->get('Access-Control-Allow-Origin'));
        self::assertSame('GET, POST, OPTIONS', $event->getResponse()->headers->get('Access-Control-Allow-Methods'));
    }

    public function testAllowedOriginIsAddedToNormalGatewayResponse(): void
    {
        putenv('COACH_CORS_ORIGINS=https://app.example');
        $_ENV['COACH_CORS_ORIGINS'] = 'https://app.example';
        $kernel = $this->createMock(HttpKernelInterface::class);
        $request = Request::create('/health', 'GET', server: ['HTTP_ORIGIN' => 'https://app.example']);
        $response = new Response('ok');
        $event = new ResponseEvent($kernel, $request, HttpKernelInterface::MAIN_REQUEST, $response);

        (new CorsSubscriber())->onResponse($event);

        self::assertSame('https://app.example', $response->headers->get('Access-Control-Allow-Origin'));
        self::assertSame('Origin', $response->headers->get('Vary'));
    }

    public function testDisallowedPreflightFailsClosed(): void
    {
        putenv('COACH_CORS_ORIGINS=https://allowed.example');
        $_ENV['COACH_CORS_ORIGINS'] = 'https://allowed.example';
        $request = Request::create('/api/v1/coach', 'OPTIONS', server: ['HTTP_ORIGIN' => 'https://other.example']);
        $event = new RequestEvent($this->createMock(HttpKernelInterface::class), $request, HttpKernelInterface::MAIN_REQUEST);

        (new CorsSubscriber())->onRequest($event);

        self::assertFalse($event->hasResponse());
    }
}
