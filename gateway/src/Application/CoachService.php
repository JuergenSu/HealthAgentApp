<?php
namespace App\Application;
use App\Contract\CoachRequest;use App\Contract\CoachResponse;use App\OpenAI\OpenAiClient;
final readonly class CoachService { public function __construct(private OpenAiClient $client) {} public function respond(CoachRequest $request): CoachResponse { return $this->client->respond($request); } }
