<?php
namespace App\OpenAI;
use App\Contract\CoachRequest;
use App\Contract\CoachResponse;
use App\Exception\UpstreamUnavailableException;
final class UnavailableOpenAiClient implements OpenAiClient {
 public function respond(CoachRequest $request): CoachResponse { throw new UpstreamUnavailableException('OpenAI integration is not configured yet.'); }
}
