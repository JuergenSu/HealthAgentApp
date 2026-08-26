<?php
namespace App\OpenAI;
use App\Contract\CoachRequest;
use App\Contract\CoachResponse;
interface OpenAiClient { public function respond(CoachRequest $request): CoachResponse; }
