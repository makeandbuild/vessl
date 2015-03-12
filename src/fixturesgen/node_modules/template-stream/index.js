// template-stream
xtend  = require('xtend') // extend object like a boss. Raynos/xtend
util   = require('util')
stream = require('stream').Transform
if (!stream) stream = require('readable-stream').Transform // stream 2 compatible

// json string in and out
function tStream(config) {
	self = this
	self._config  = (config) ? config : {}
	self._default = { jsonOut: 'auto', filterErrors: false, appendString:'' } // true/false/'auto'=same as came in
	self._stat    = {
		processed: 0,
		filtered:  0,
		badJson:   0
	}
	self.config   = xtend(self._default, self._config)

	self.bump = function(area) {
		self._stat[area] = (self._stat[area]) ? self._stat[area] + 1 : 1
	}

	self.stat = function(area) {
		if (self._stat[area]) {
			return self._stat[area]
		} else {
			return self._stat
		}
	}

	self.Default = function(config) {
		self._default = xtend(self._default, self.config)  // update defaults
		self.config   = xtend(self._default, self._config) // remerge defaults with original config
		return self
	} 

	self.bind = function(area, fn) {
		self[area] = fn
		return self
	}

	self.onError = function(err) { 
		self.emit('error', err)
		return self
	}

	stream.call(self, { objectMode: true })

	self.onFlush = function(cb) { return true } // true=do callback, false=don't callback
	self._flush  = function(cb) {
		if (self.onFlush(cb)) cb()
	}

	self.onTransform = function (data, callback) {
		callback(data) // return undefined to filter
	}


	self._transform = function (data, encoding, callback) {
		if (data) {
			isJson  = data instanceof Buffer

			function completeTransform(data) {
				if (data === undefined) {
					self._stat.filtered++
				} else {
					if (self.config.jsonOut || (isJson && self.config.jsonOut == 'auto')) {
						json = JSON.stringify(data) + self.config.appendString
						data = new Buffer(json, 'utf8')
					}
					self.push(data)
					self._stat.processed++
				}
				callback()
			}

			if (isJson) {
				json = data.toString('utf8')
				try {
					data = JSON.parse(json)
					self.onTransform(data, completeTransform)
				} catch (e) {
					self._stat.badJson++
					if (!self.config.filterErrors) self.emit('error', e + ': ' + json)
					self.push(data)
					callback()
				}
			}
		} else {
			self.push(data)
			callback()
		}
	};
}


util.inherits(tStream, stream)
module.exports = tStream