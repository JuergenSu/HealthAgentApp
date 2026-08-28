<?php
declare(strict_types=1);

$projectDir = __DIR__;
require $projectDir.'/vendor/autoload.php';

App\Runtime\WebspaceFrontController::run($projectDir);
