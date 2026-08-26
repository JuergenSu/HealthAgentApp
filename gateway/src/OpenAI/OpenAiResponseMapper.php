<?php
namespace App\OpenAI;

use App\Agent\ToolRegistry;use App\Contract\CoachResponse;use App\Exception\InvalidModelResponseException;
final class OpenAiResponseMapper {
 public function map(array $raw,?string $outputText):CoachResponse{try{foreach(($raw['output']??[])as$item){if(($item['type']??null)!=='function_call')continue;$id=$item['call_id']??$item['id']??null;$name=$item['name']??null;$argumentsRaw=$item['arguments']??null;if(!is_string($id)||!is_string($name)||!is_string($argumentsRaw))throw new \InvalidArgumentException('invalid tool call from model');$arguments=json_decode($argumentsRaw,true,512,JSON_THROW_ON_ERROR);if(!is_array($arguments))throw new \InvalidArgumentException('tool arguments must be object');return CoachResponse::toolCall($id,$name,ToolRegistry::validate($name,$arguments));}if($outputText===null||trim($outputText)==='')throw new \InvalidArgumentException('model returned neither final text nor tool call');$payload=json_decode($outputText,true,512,JSON_THROW_ON_ERROR);if(!is_array($payload))throw new \InvalidArgumentException('invalid final JSON');return CoachResponse::final($payload);}catch(\Throwable $e){if($e instanceof InvalidModelResponseException)throw $e;throw new InvalidModelResponseException('Invalid structured model output',0,$e);}}
}
