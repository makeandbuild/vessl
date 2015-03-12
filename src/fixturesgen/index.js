#!/usr/bin/env node

var argv = require('optimist')
	.usage('Usage: $0 --source [string] --start [int] --count [int] --input [string] --output [string] --paramDir [string]')
	.demand(['source', 'count', 'start', 'paramDir'])
	.argv;

var TemplateStream = require('template-stream').TemplateStream;
var Handlebars = require("handlebars");
var fs = require('fs')
var moment = require("moment")
var _ = require("underscore")

Handlebars.registerHelper('rand', function(items) {
	var index = Math.floor(Math.random() * (items.length));
	return items[index];
});

Handlebars.registerHelper('durationUtil', function(durationString, block) {
	var duration = moment.duration(durationString);
	if (block.hash.action == "subtract") {
		return moment().subtract(duration).toISOString();
	} else {
		return moment().add(duration).toISOString();
	}
});

var source = fs.readFileSync(argv.source, {
	encoding: "UTF-8"
});

var template = Handlebars.compile(source);

function loadSources() {
	var base = {};
	_.each(fs.readdirSync(argv.paramDir), function(file){
		var name = file.substr(0, file.indexOf("."));
		base[name] = require(argv.paramDir + "/" +name)
	});
	return base;
}
var base = loadSources();
if (argv.input == "stream") {
	var JSONStream = require('JSONStream');
	var parsestream = JSONStream.parse('.*')
	var overallIndex = 0;
	var out = process.stdout;
	out.write("[");
	var end = argv.start + argv.count;
	parsestream.on('data', function(data) {
		for (var i = argv.start; i < end; i++) {
			if (overallIndex > 0)
				out.write(",\n");
			var json = template(_.extend({
				index: i,
				input: data,
				overallIndex: overallIndex
			}, base));
			overallIndex++;
			out.write(json);
		}
	});
	parsestream.on('end', function(data) {
		out.write("]");
	});
	process.stdin.pipe(parsestream);
} else {
	process.stdout.write("[");
	var end = argv.start + argv.count;
	for (var i = argv.start; i < end; i++) {
		if (i > argv.start)
			process.stdout.write(",\n");
		var json = template(_.extend({
			index: i
		}, base));
		process.stdout.write(json);
	}
	process.stdout.write("]");
}