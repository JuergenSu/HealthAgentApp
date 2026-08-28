<?php
namespace App\Contract;

final readonly class CoachRequest {
    private const CONTEXT_FIELDS = ['profile','goal','today','recentTraining','baselines','memories'];

    public function __construct(
        public string $message,
        public ?string $conversationId = null,
        public array $context = [],
        public array $conversation = [],
        public array $toolResults = []
    ) {}

    public static function fromArray(array $data): self {
        $known = ['message','conversationId','context','conversation','toolResults'];
        foreach (array_keys($data) as $key) if (!in_array($key, $known, true)) throw new \InvalidArgumentException('unknown request field');
        if (!isset($data['message']) || !is_string($data['message']) || trim($data['message']) === '' || mb_strlen($data['message']) > 4000) throw new \InvalidArgumentException('invalid message');
        if (isset($data['conversationId']) && (!is_string($data['conversationId']) || mb_strlen($data['conversationId']) > 100)) throw new \InvalidArgumentException('invalid conversationId');
        foreach (['context','conversation','toolResults'] as $field) if (isset($data[$field]) && !is_array($data[$field])) throw new \InvalidArgumentException('invalid '.$field);
        $context = $data['context'] ?? [];
        foreach (array_keys($context) as $key) if (!in_array($key, self::CONTEXT_FIELDS, true)) throw new \InvalidArgumentException('unknown context field');
        $conversation = $data['conversation'] ?? [];
        if (count($conversation) > 12) throw new \InvalidArgumentException('conversation too long');
        foreach ($conversation as $message) {
            if (!is_array($message) || !isset($message['role'],$message['content']) || !in_array($message['role'], ['user','assistant'], true) || !is_string($message['content']) || mb_strlen($message['content']) > 4000) throw new \InvalidArgumentException('invalid conversation message');
        }
        $toolResults = $data['toolResults'] ?? [];
        if (count($toolResults) > 8) throw new \InvalidArgumentException('too many tool results');
        foreach ($toolResults as $result) {
            if (!is_array($result) || !isset($result['id'],$result['name'],$result['result']) || !is_string($result['id']) || !is_string($result['name']) || !is_array($result['result'])) throw new \InvalidArgumentException('invalid tool result');
        }
        return new self(trim($data['message']), $data['conversationId'] ?? null, $context, $conversation, $toolResults);
    }

    public function toModelInput(): array {
        return [
            'conversationId' => $this->conversationId,
            'userMessage' => $this->message,
            'context' => $this->context,
            'conversation' => $this->conversation,
            'toolResults' => $this->toolResults,
        ];
    }
}
