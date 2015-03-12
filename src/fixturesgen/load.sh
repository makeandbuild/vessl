export OUTPUT=../test/resources/fixturesgen
mkdir -p $OUTPUT

./index.js --source ./templates/com.makeandbuild.vessl.persistence.User.hbs --count 10000 --start 1000000 --paramDir ./paramDir> $OUTPUT/com.makeandbuild.vessl.persistence.User.json
