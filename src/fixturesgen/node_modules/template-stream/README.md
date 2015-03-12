# template-stream

Template Stream

## Install

```sh
npm install template-stream
```

## Usage

Building your own streams

```js
tStream = new require('template-stream')({ jsonOut: 'auto', filterErrors: false, appendString: '' }) // default config
tStream.bind('onTransform', function(data) {
	// do your processing here
	return data // return data to be pushed down stream, return undefined to filter the data
})
```

## Config

```jsonOut``` json true/false string output down stream. 'auto' = same form as upstream
```filterErrors``` false=json parse errors emit the error, true=json parse errors are silently filtered

## Statistics

```js
// all
console.log(tStream.stat()) // { processed: 942, filtered: 18, badJson: 2 }

// particular statistic
console.log(tStream.stat('filtered')) // 18

// your own statistics
tStream.bump('myStat')
console.log(tStream.stat()) // { processed: 942, filtered: 18, badJson: 2, myStat: 1 }
```

## License

### MIT
