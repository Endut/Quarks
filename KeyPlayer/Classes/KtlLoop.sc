/****

// assume all events are parameter set actions,
// provide simple facilites for that.
// the ones that are numerical can be rescaled and shifted

/* Questions:
   push currState to object when changing rescaling?
*/

*****/

KtlLoop : EventLoop {

	var <>paramsToRecord, <currState;
	var <>object, <>ktlMap;

	var <>scaler=1, <>shift=0.0,
	<>rescaled=false, <nonRescalableCtls, inverter=1;

	isInverse { ^inverter == -1 }
	invert { inverter = -1 }
	up { inverter = 1 }
	flipInv { inverter = inverter.neg }

	dontRescale { arg ... ids; nonRescalableCtls.addAll(ids.flat); }
	doRescale { arg ... ids; nonRescalableCtls.removeAll(ids.flat); }
	resetScaling { scaler = 1; shift = 0; inverter = 1; }

	init {
		super.init;
		paramsToRecord = [];
		currState = ();
		ktlMap = ();
		nonRescalableCtls = [];
	}

	rescaleVal { |val, clip = true|
		var scaled = val - 0.5 * scaler + shift * inverter + 0.5;
		if (clip) { scaled = scaled.clip(0, 1) };
		^scaled
	}

	rescaleEvent { |event|
		paramsToRecord.removeAll(nonRescalableCtls).do { |par|
			var val = event[par];
			if (val.notNil) {
				val = this.rescaleVal(val);
				event.put(par, val);
			};
		};
		^event
	}

	setuObj {|event|
		var parEvent = event.select({ |val, key|
			paramsToRecord.includes(key);
		});

		if (rescaled) { parEvent = this.rescaleEvent(parEvent) };

		currState.putAll(parEvent);
		parEvent.keysValuesDo { |par, val|
			object.setu(ktlMap[par], val);
		}
	}

	defaultFunc {
		^{  |ev| if (ev[\type] == \set) { this.setuObj(ev); } };
	}

	postKtlInfo {
		var str =
		(this.asString ++ ".paramsToRecord: (%);\n"
		    .format(paramsToRecord.asCompileString) +
		this.asString ++ ".nonRescalableCtls: (%);\n"
		    .format(nonRescalableCtls.asCompileString) +
		this.asString ++ ".ktlMap: (%);\n")
		    .format(ktlMap.asCompileString);
		str.newTextWindow(this.class.asString ++ "_" ++ this.key ++ "_settings");
		^str
	}
}

KtlLoopGui : EventLoopGui {


	// add sliders for scaler, shift,
	// but rescaled, inv, rstScale;
	// show ktlMap, paramsToRecord, nonRescalableCtls?

	makeViews { |options|
		super.makeViews(options);
		zone.decorator.nextLine.shift(0, 5);
		this.addScaleItems(zone.bounds.width - 8, skin.buttonHeight);
	}

	addScaleItems { |lineWidth, height|

		var resetScaleBut, sclBut, invBut, infoBut, scalerSl, shiftSl;
		zone.decorator.nextLine;

		resetScaleBut = Button(zone, Rect(0,0,lineWidth * 0.2,height))
			.font_(font)
			.states_([[\rsScale]])
			.action_({ |but| object.resetLoop });

		zone.decorator.shift(5, 0);

		sclBut = Button(zone, Rect(0,0, lineWidth * 0.13,height))
			.font_(font)
			.states_([
				[\rscl, skin.fontColor, skin.offColor],
				["norm", skin.fontColor, skin.onColor]
			])
			.action_({ |but| object.rescaled_(but.value > 0); });


		invBut = Button(zone, Rect(0,0, lineWidth * 0.13,height))
			.font_(font)
			.states_([
				[\inv, skin.fontColor, skin.offColor],
				[\up, skin.fontColor, skin.onColor]
			])
			.action_({ |but|
				[ { object.invert }, { object.up } ][but.value].value;
			});

		zone.decorator.shift(10, 0);

		infoBut = Button(zone, Rect(0,0, lineWidth * 0.2,height))
			.font_(font)
			.states_([[\info]])
		.action_({ object.postKtlInfo });
		scalerSl = EZSlider(zone, lineWidth@height, \scale, [0.0, 4, \amp],
			{ |sl| object.scaler = sl.value }, 1, labelWidth: 40)
		.font_(font);

		shiftSl = EZSlider(zone, lineWidth@height, \shift, [-0.5, 0.5],
			{ |sl| object.shift = sl.value }, 0, labelWidth: 40)
		.font_(font);
	}

	getState {
		var newState = super.getState;
		if (object.notNil) {
			newState.put(\scaler, object.scaler);
			newState.put(\inverted, object.isInverse);
			newState.put(\shift, object.shift);
		};
		^newState
	}
}

