<?php
namespace App\Contract;
final readonly class CoachResponse {
 public function __construct(public string $type, public ?string $message = null, public array $suggestedActions = [], public ?array $toolCall = null) {}
 public function toArray(): array { return array_filter(['type'=>$this->type,'message'=>$this->message,'suggestedActions'=>$this->suggestedActions,'toolCall'=>$this->toolCall], static fn($v)=>$v!==null); }
}
