COVERALLS_URL='https://coveralls.io/api/v1/jobs'
CLOVERAGE_VERSION='1.0.4' lein cloverage -o target/coverage --coveralls
curl -F 'json_file=@target/coverage/coveralls.json' "$COVERALLS_URL"
