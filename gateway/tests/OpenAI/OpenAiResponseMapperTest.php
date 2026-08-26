<?php
namespace App\Tests\OpenAI;

use App\Exception\InvalidModelResponseException;use App\OpenAI\OpenAiResponseMapper;use PHPUnit\Framework\TestCase;

final class OpenAiResponseMapperTest extends TestCase {
 public function testMapsGermanFinalFixture():void{$fixture=json_decode(file_get_contents(__DIR__.'/../../../docs/agent-contract-fixtures/final.json'),true,512,JSON_THROW_ON_ERROR);$response=(new OpenAiResponseMapper())->map(['output'=>[]],json_encode($fixture,JSON_THROW_ON_ERROR));self::assertSame('FINAL',$response->type);self::assertStringContainsString('Heute',$response->message);}
 public function testMapsValidReadToolCall():void{$raw=['output'=>[['type'=>'function_call','call_id'=>'call_1','name'=>'get_today_status','arguments'=>'{}']]];$r=(new OpenAiResponseMapper())->map($raw,null);self::assertSame('TOOL_CALL',$r->type);self::assertSame('get_today_status',$r->toolCall['name']);}
 public function testMapsValidWriteToolCall():void{$raw=['output'=>[['type'=>'function_call','call_id'=>'call_2','name'=>'reschedule_workout','arguments'=>'{"workoutId":12,"date":"2026-08-28"}']]];$r=(new OpenAiResponseMapper())->map($raw,null);self::assertSame('reschedule_workout',$r->toolCall['name']);}
 public function testRejectsUnknownTool():void{$this->expectException(InvalidModelResponseException::class);(new OpenAiResponseMapper())->map(['output'=>[['type'=>'function_call','call_id'=>'x','name'=>'read_entire_room_database','arguments'=>'{}']]],null);}
 public function testRejectsMalformedFinal():void{$this->expectException(InvalidModelResponseException::class);(new OpenAiResponseMapper())->map(['output'=>[]],'{"type":"FINAL"}');}
}
