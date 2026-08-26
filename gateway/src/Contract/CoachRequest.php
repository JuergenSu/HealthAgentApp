<?php
namespace App\Contract;
final readonly class CoachRequest {
 public function __construct(public string $message, public ?string $conversationId = null, public array $context = [], public array $toolResults = []) {}
 public static function fromArray(array $data): self {
  if (!isset($data['message']) || !is_string($data['message']) || trim($data['message']) === '' || mb_strlen($data['message']) > 4000) throw new \InvalidArgumentException('message must be a non-empty string with at most 4000 characters');
  foreach (['context','toolResults'] as $field) if (isset($data[$field]) && !is_array($data[$field])) throw new \InvalidArgumentException($field.' must be an object/array');
  if (isset($data['conversationId']) && !is_string($data['conversationId'])) throw new \InvalidArgumentException('conversationId must be a string');
  return new self(trim($data['message']), $data['conversationId'] ?? null, $data['context'] ?? [], $data['toolResults'] ?? []);
 }
}
