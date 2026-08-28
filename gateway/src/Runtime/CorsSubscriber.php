<?php
declare(strict_types=1);

namespace App\Runtime;

use Symfony\Component\EventDispatcher\EventSubscriberInterface;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpKernel\Event\RequestEvent;
use Symfony\Component\HttpKernel\Event\ResponseEvent;
use Symfony\Component\HttpKernel\KernelEvents;

final class CorsSubscriber implements EventSubscriberInterface
{
    public static function getSubscribedEvents(): array
    {
        return [
            KernelEvents::REQUEST => ['onRequest', 1000],
            KernelEvents::RESPONSE => ['onResponse', -1000],
        ];
    }

    public function onRequest(RequestEvent $event): void
    {
        if (!$event->isMainRequest()) {
            return;
        }
        $request = $event->getRequest();
        if ($request->getMethod() !== Request::METHOD_OPTIONS || !$this->isGatewayPath($request->getPathInfo())) {
            return;
        }
        $allowedOrigin = $this->allowedOrigin($request->headers->get('Origin'));
        if ($allowedOrigin === null) {
            return;
        }
        $response = new Response('', 204);
        $this->applyHeaders($response, $allowedOrigin);
        $event->setResponse($response);
    }

    public function onResponse(ResponseEvent $event): void
    {
        if (!$event->isMainRequest()) {
            return;
        }
        $request = $event->getRequest();
        if (!$this->isGatewayPath($request->getPathInfo())) {
            return;
        }
        $allowedOrigin = $this->allowedOrigin($request->headers->get('Origin'));
        if ($allowedOrigin !== null) {
            $this->applyHeaders($event->getResponse(), $allowedOrigin);
        }
    }

    private function allowedOrigin(?string $origin): ?string
    {
        if ($origin === null || trim($origin) === '') {
            return null;
        }
        $configured = trim((string) SharedHostingConfig::value('COACH_CORS_ORIGINS'));
        if ($configured === '') {
            return null;
        }
        if ($configured === '*') {
            return '*';
        }
        $allowed = array_filter(array_map('trim', explode(',', $configured)));
        return in_array($origin, $allowed, true) ? $origin : null;
    }

    private function applyHeaders(Response $response, string $origin): void
    {
        $response->headers->set('Access-Control-Allow-Origin', $origin);
        $response->headers->set('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
        $response->headers->set('Access-Control-Allow-Headers', 'Content-Type');
        $response->headers->set('Access-Control-Max-Age', '600');
        if ($origin !== '*') {
            $response->headers->set('Vary', 'Origin');
        }
    }

    private function isGatewayPath(string $path): bool
    {
        return $path === '/health' || $path === '/api/v1/coach';
    }
}
