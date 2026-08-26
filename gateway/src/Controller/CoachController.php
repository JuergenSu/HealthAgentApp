<?php
namespace App\Controller;
use App\Application\CoachService;
use App\Contract\CoachRequest;
use App\Exception\InvalidModelResponseException;
use App\Exception\UpstreamUnavailableException;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;
final readonly class CoachController {
 public function __construct(private CoachService $coach) {}
 #[Route('/api/v1/coach', name:'coach', methods:['POST'])]
 public function __invoke(Request $request): JsonResponse {
  try {
   $data=json_decode($request->getContent(),true,512,JSON_THROW_ON_ERROR);
   if(!is_array($data)) throw new \InvalidArgumentException('JSON object expected');
   return new JsonResponse($this->coach->respond(CoachRequest::fromArray($data))->toArray());
  } catch (\JsonException|\InvalidArgumentException $e) {
   return new JsonResponse(['error'=>['code'=>'INVALID_REQUEST','message'=>'Ungültige Anfrage.']],400);
  } catch (InvalidModelResponseException $e) {
   return new JsonResponse(['error'=>['code'=>'INVALID_MODEL_RESPONSE','message'=>'Der Coach hat eine ungültige strukturierte Antwort geliefert.']],502);
  } catch (UpstreamUnavailableException $e) {
   return new JsonResponse(['error'=>['code'=>'UPSTREAM_UNAVAILABLE','message'=>'Der Coach-Dienst ist vorübergehend nicht verfügbar.']],503);
  } catch (\Throwable $e) {
   return new JsonResponse(['error'=>['code'=>'INTERNAL_ERROR','message'=>'Interner Gateway-Fehler.']],500);
  }
 }
}
