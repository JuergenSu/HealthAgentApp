<?php
declare(strict_types=1);

namespace App\Agent;

final class ToolRegistry
{
    public const READ = [
        'get_today_status',
        'get_health_summary',
        'get_training_plan',
        'get_workout_history',
        'get_active_goal',
        'get_baseline',
        'get_progress',
    ];

    public const WRITE = [
        'modify_workout',
        'reschedule_workout',
        'skip_workout',
        'record_checkin',
    ];

    public static function definitions(): array
    {
        return [
            self::tool('get_today_status', 'Liest den heutigen Recovery- und Trainingsstatus.', []),
            self::tool('get_health_summary', 'Liest eine zusammengefasste Gesundheitsübersicht.', [
                'days' => self::integer(1, 90),
            ]),
            self::tool('get_training_plan', 'Liest den Trainingsplan einer Woche.', [
                'weekStart' => self::string(),
            ]),
            self::tool('get_workout_history', 'Liest die jüngste Trainingshistorie.', [
                'days' => self::integer(1, 90),
            ]),
            self::tool('get_active_goal', 'Liest das aktive Trainingsziel.', []),
            self::tool('get_baseline', 'Liest eine persönliche Baseline.', [
                'metric' => self::enum(['sleepMinutes', 'restingHeartRate', 'steps', 'distanceKm', 'trainingLoad']),
                'windowDays' => self::enum([7, 28, 90]),
            ], ['metric', 'windowDays']),
            self::tool('get_progress', 'Liest eine kompakte Fortschrittsübersicht.', [
                'days' => self::integer(7, 90),
            ]),
            self::tool('modify_workout', 'Fordert eine lokal validierte Änderung einer geplanten Einheit an.', [
                'workoutId' => self::integer(1, null),
                'durationMinutes' => self::integer(0, 300),
                'workoutType' => self::enum(['EASY', 'LONG', 'INTERVAL', 'TEMPO', 'RECOVERY', 'STRENGTH', 'MOBILITY', 'REST']),
            ], ['workoutId']),
            self::tool('reschedule_workout', 'Fordert eine lokal validierte Verschiebung einer Einheit an.', [
                'workoutId' => self::integer(1, null),
                'date' => self::string(),
            ], ['workoutId', 'date']),
            self::tool('skip_workout', 'Markiert eine geplante Einheit nach lokaler Validierung als übersprungen.', [
                'workoutId' => self::integer(1, null),
                'reason' => self::string(),
            ], ['workoutId']),
            self::tool('record_checkin', 'Speichert einen subjektiven Tages-Check-in lokal.', [
                'date' => self::string(),
                'energy' => self::integer(1, 5),
                'muscleFatigue' => self::integer(1, 5),
                'motivation' => self::integer(1, 5),
                'stress' => self::integer(1, 5),
            ], ['date']),
        ];
    }

    public static function validate(string $name, array $arguments): array
    {
        $definition = null;
        foreach (self::definitions() as $tool) {
            if ($tool['name'] === $name) {
                $definition = $tool;
                break;
            }
        }
        if ($definition === null) {
            throw new \InvalidArgumentException('unknown tool');
        }

        $schema = $definition['parameters'];
        $properties = (array) $schema['properties'];

        foreach ($schema['required'] as $key) {
            if (!array_key_exists($key, $arguments)) {
                throw new \InvalidArgumentException('missing tool argument');
            }
        }

        foreach ($arguments as $key => $value) {
            if (!isset($properties[$key])) {
                throw new \InvalidArgumentException('unknown tool argument');
            }
            $property = $properties[$key];
            if (($property['type'] ?? null) === 'integer' && !is_int($value)) {
                throw new \InvalidArgumentException('invalid integer argument');
            }
            if (($property['type'] ?? null) === 'string' && !is_string($value)) {
                throw new \InvalidArgumentException('invalid string argument');
            }
            if (isset($property['minimum']) && $value < $property['minimum']) {
                throw new \InvalidArgumentException('argument below minimum');
            }
            if (isset($property['maximum']) && $value > $property['maximum']) {
                throw new \InvalidArgumentException('argument above maximum');
            }
            if (isset($property['enum']) && !in_array($value, $property['enum'], true)) {
                throw new \InvalidArgumentException('argument outside enum');
            }
        }

        return $arguments;
    }

    private static function tool(string $name, string $description, array $properties, array $required = []): array
    {
        // PHP encodes [] as a JSON array. JSON Schema requires `properties` to
        // always be an object, including for functions without arguments.
        $jsonProperties = $properties === [] ? new \stdClass() : $properties;

        return [
            'type' => 'function',
            'name' => $name,
            'description' => $description,
            'strict' => false,
            'parameters' => [
                'type' => 'object',
                'properties' => $jsonProperties,
                'required' => $required,
                'additionalProperties' => false,
            ],
        ];
    }

    private static function integer(?int $min, ?int $max): array
    {
        $schema = ['type' => 'integer'];
        if ($min !== null) {
            $schema['minimum'] = $min;
        }
        if ($max !== null) {
            $schema['maximum'] = $max;
        }
        return $schema;
    }

    private static function string(): array
    {
        return ['type' => 'string'];
    }

    private static function enum(array $values): array
    {
        return [
            'type' => is_int($values[0]) ? 'integer' : 'string',
            'enum' => $values,
        ];
    }
}
