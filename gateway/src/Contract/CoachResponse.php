<?php
namespace App\Contract;

final readonly class CoachResponse {
    public function __construct(
        public string $type,
        public ?string $message = null,
        public array $suggestedActions = [],
        public ?array $memoryCandidate = null,
        public ?array $toolCall = null
    ) {
        if (!in_array($type, ['FINAL','TOOL_CALL'], true)) throw new \InvalidArgumentException('invalid response type');
        if ($type === 'FINAL' && ($message === null || trim($message) === '')) throw new \InvalidArgumentException('FINAL requires message');
        if ($type === 'TOOL_CALL' && $toolCall === null) throw new \InvalidArgumentException('TOOL_CALL requires toolCall');
    }

    public static function final(array $payload): self {
        if (($payload['type'] ?? null) !== 'FINAL' || !isset($payload['message']) || !is_string($payload['message'])) throw new \InvalidArgumentException('invalid FINAL');
        $actions = $payload['suggestedActions'] ?? [];
        if (!is_array($actions) || count($actions) > 4) throw new \InvalidArgumentException('invalid suggestedActions');
        foreach ($actions as $action) if (!is_string($action) || mb_strlen($action) > 120) throw new \InvalidArgumentException('invalid suggestedAction');
        $memory = $payload['memoryCandidate'] ?? null;
        if ($memory !== null && (!is_array($memory) || !isset($memory['text']) || !is_string($memory['text']))) throw new \InvalidArgumentException('invalid memoryCandidate');
        return new self('FINAL', trim($payload['message']), $actions, $memory, null);
    }

    public static function toolCall(string $id, string $name, array $arguments): self {
        return new self('TOOL_CALL', null, [], null, ['id'=>$id,'name'=>$name,'arguments'=>$arguments]);
    }

    public function toArray(): array {
        return array_filter([
            'type'=>$this->type,
            'message'=>$this->message,
            'suggestedActions'=>$this->suggestedActions,
            'memoryCandidate'=>$this->memoryCandidate,
            'toolCall'=>$this->toolCall
        ], static fn($v)=>$v!==null);
    }
}
