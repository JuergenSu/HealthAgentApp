<?php
namespace App\Tests\Contract;
use App\Contract\CoachRequest;use PHPUnit\Framework\TestCase;
final class CoachRequestTest extends TestCase {
 public function testValidAllowListedRequest():void{$r=CoachRequest::fromArray(['message'=>'Wie soll ich heute trainieren?','context'=>['today'=>['recovery'=>['score'=>80]],'goal'=>['title'=>'10 km']],'conversation'=>[['role'=>'user','content'=>'Wie war gestern?']]]);self::assertSame('Wie soll ich heute trainieren?',$r->message);self::assertArrayHasKey('today',$r->context);}
 public function testEmptyMessageRejected():void{$this->expectException(\InvalidArgumentException::class);CoachRequest::fromArray(['message'=>'   ']);}
 public function testInvalidContextTypeRejected():void{$this->expectException(\InvalidArgumentException::class);CoachRequest::fromArray(['message'=>'Hallo','context'=>'raw']);}
 public function testUnknownContextSectionRejected():void{$this->expectException(\InvalidArgumentException::class);CoachRequest::fromArray(['message'=>'Hallo','context'=>['rawHeartRateSeries'=>[1,2,3]]]);}
 public function testUnlimitedConversationRejected():void{$conversation=[];for($i=0;$i<13;$i++)$conversation[]=['role'=>'user','content'=>'x'];$this->expectException(\InvalidArgumentException::class);CoachRequest::fromArray(['message'=>'Hallo','conversation'=>$conversation]);}
}
