<?php
namespace App\OpenAI;

use App\Agent\ToolRegistry;
use App\Contract\CoachRequest;
use App\Contract\CoachResponse;
use App\Exception\UpstreamUnavailableException;
use OpenAI;
use Psr\Log\LoggerInterface;

final class OpenAiResponsesClient implements OpenAiClient {
    private const PROMPT_VERSION = 'coach-v1';

    public function __construct(
        private readonly string $apiKey,
        private readonly string $model,
        private readonly string $promptPath,
        private readonly OpenAiResponseMapper $mapper,
        private readonly LoggerInterface $logger
    ) {}

    public function respond(CoachRequest $request): CoachResponse {
        if (trim($this->apiKey) === '') throw new UpstreamUnavailableException('OpenAI API key missing');
        $prompt = @file_get_contents($this->promptPath);
        if ($prompt === false) throw new \RuntimeException('Coach prompt missing');
        try {
            $client = OpenAI::client($this->apiKey);
            $response = $client->responses()->create([
                'model' => $this->model,
                'instructions' => $prompt,
                'input' => json_encode($request->toModelInput(), JSON_THROW_ON_ERROR | JSON_UNESCAPED_UNICODE),
                'tools' => ToolRegistry::definitions(),
                'text' => ['format' => self::finalFormat()],
                'store' => false,
            ]);
            $this->logger->info('coach_openai_response', [
                'model' => $this->model,
                'prompt_version' => self::PROMPT_VERSION,
                'conversation_id' => $request->conversationId,
                'tool_result_count' => count($request->toolResults),
            ]);
            return $this->mapper->map($response->toArray(), $response->outputText ?? null);
        } catch (\InvalidArgumentException|\JsonException $e) {
            throw $e;
        } catch (\Throwable $e) {
            $this->logger->warning('coach_openai_upstream_error', ['model'=>$this->model,'prompt_version'=>self::PROMPT_VERSION,'exception'=>$e::class]);
            throw new UpstreamUnavailableException('OpenAI request failed', 0, $e);
        }
    }

    private static function finalFormat(): array {
        return [
            'type' => 'json_schema',
            'name' => 'coach_final',
            'strict' => true,
            'schema' => [
                'type' => 'object',
                'properties' => [
                    'type' => ['type'=>'string','enum'=>['FINAL']],
                    'message' => ['type'=>'string'],
                    'suggestedActions' => ['type'=>'array','items'=>['type'=>'string'],'maxItems'=>4],
                    'memoryCandidate' => [
                        'anyOf' => [
                            ['type'=>'object','properties'=>['text'=>['type'=>'string']],'required'=>['text'],'additionalProperties'=>false],
                            ['type'=>'null']
                        ]
                    ],
                ],
                'required' => ['type','message','suggestedActions','memoryCandidate'],
                'additionalProperties' => false,
            ],
        ];
    }
}
