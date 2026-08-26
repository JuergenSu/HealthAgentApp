<?php
namespace App\Tests\Contract;
use App\Contract\CoachRequest;use PHPUnit\Framework\TestCase;
final class CoachRequestTest extends TestCase { public function testValidRequest():void{$r=CoachRequest::fromArray(['message'=>'Wie soll ich heute trainieren?','context'=>['recovery'=>['score'=>80]]]);self::assertSame('Wie soll ich heute trainieren?',$r->message);} public function testEmptyMessageRejected():void{$this->expectException(\InvalidArgumentException::class);CoachRequest::fromArray(['message'=>'   ']);} public function testInvalidContextRejected():void{$this->expectException(\InvalidArgumentException::class);CoachRequest::fromArray(['message'=>'Hallo','context'=>'raw']);} }
