<?php
namespace App\Tests\OpenAI;
use PHPUnit\Framework\TestCase;
final class CoachPromptTest extends TestCase{public function testPromptRequiresGermanAndMissingIsNotZero():void{$p=file_get_contents(__DIR__.'/../../resources/prompts/coach-v1.txt');self::assertStringContainsString('standardmäßig auf Deutsch',$p);self::assertStringContainsString('Fehlende Daten sind unbekannt und niemals als 0',$p);self::assertStringContainsString('keine medizinischen Diagnosen',$p);self::assertStringContainsString('dürfen nicht umgangen werden',$p);}}
