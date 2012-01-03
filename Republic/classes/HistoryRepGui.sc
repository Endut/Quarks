// limit number of lines to show to a maximum?

HistoryRepGui : JITGui {

	classvar <>docTitle = "History repeats", <>docHeight=120;

	var <w, <textV;
	var <startBut, <filtBut, <filTextV, <filtBut, <keyPop, <topBut, listV;
	var <doc, <oldDocs, <docFlag = \sameDoc, <>stickMode=0;

	var <filters, <filteredIndices, <filteredShorts, <filtering = false;
	var lastLineSelected = 0, lastLinesShown;

	accepts { |obj| ^obj.isNil or: { obj.isKindOf(History) } }

		// these methods should be overridden in subclasses: 
	setDefaults { |options|
		if (parent.isNil) { 
			defPos = 10@260
		} { 
			defPos = skin.margin;
		};
		minSize = 300 @ 400;
	//	"minSize: %\n".postf(minSize);
	}

	makeViews { |options|
		var font, flow;

		font = Font("Osaka", 9);				////
		flow = zone.addFlowLayout(2@2, 1@1);
		this.name_("History");
				
		filters = [\all, ""];
		
		textV = TextView(zone, Rect(0, 0, (zone.bounds.width - 4).postln, numItems * 12 + 2))
			.string_("")
			.enterInterpretsSelection_(false)
			.keyDownAction_({ |txvw, char, mod, uni, keycode|
				// char.postcs;
				if ([3, 13].includes(char.ascii)) {
					this.rip(textV.string);
				};
			})
			.resize_(2);

			// to do: disable if history is not current!
		startBut = Button(zone, Rect(0, 0, 50, 20)) ////
			.states_([ ["start"], ["end"]])
			.canFocus_(false)
			.action_({ |btn|
				switch(btn.value,
					0, { if (object == History.current) { History.end } },
					1, { if (object == History.current) { History.start } }
				);
			});

		filtBut = Button(zone, Rect(0, 0, 32, 20)) ////
			.canFocus_(false)
			.states_([["all"], ["filt"]]).action_({ |btn|
				this.filtering_(btn.value > 0);
				if (filtering) { this.filterLines };
				 object.hasMovedOn = true;
			});

		keyPop = PopUpMenu(zone, Rect(0, 0, 40, 20))
			.items_([\all]).value_(0)
			.action_({ |pop| this.setKeyFilter(pop.items[pop.value]) });

		filTextV = TextView(zone, Rect(0,0, 88, 20)).string_("")
			.enterInterpretsSelection_(false)
			.resize_(2)
			.keyDownAction_({ |txvw, char, mod, uni, keycode|
				this.setStrFilter(txvw.string);
				if (this.filtering) { this.filterLines; }
			});
		topBut = Button(zone, Rect(0, 0, 32, 20))
			.states_([["top"], ["keep"]]).value_(0)
			.resize_(3)
			.canFocus_(false)
			.action_({ |but| this.stickMode_(but.value) });

		Button(zone, Rect(0, 0, 32, 20)) ////
			.states_([["rip"]])
			.resize_(3)
			.canFocus_(false)
			.action_({ |btn| this.rip(textV.string) });
		
		Button(zone, Rect(0,0, 16, 20))
			.states_([["v"], ["^"]])
			.resize_(3)
			.action_ { |btn| 
				var views = w.view.children; 
				var resizes = [ 
					[2, 1, 1, 1, 2, 3, 3, 3, 5], 
					[5, 7, 7, 7, 8, 9, 9, 9, 8]
				][btn.value.asInteger];
				
				views.do { |v, i| v.resize_(resizes[i]) };
			
			};
		
		listV = ListView(zone, bounds.copy.insetBy(2).height_(230))
			.font_(font)
			.items_([])
			.resize_(5)
			.background_(Color.grey(0.62))
			.action_({ |lview|
				var index = lview.value;
				if (lview.items.isEmpty) {
					"no entries yet.".postln;
				} {
					lastLineSelected = listV.items[index];
					if (filtering.not) {
						this.postInlined(index)
					} {
						this.postInlined(filteredIndices[index])
					}
				}
			})
			.enterKeyAction_({ |lview|
				var index = lview.value;
				if (filtering) { index = filteredIndices[index] };
				try {
					object.lines[index][2].postln.interpret.postln;
				//	"did execute.".postln;
				} {
					"execute line from history failed.".postln;
				};
			});
		try { parent.name_("History") };
		this.checkUpdate;
	
	}

	getState { 
		var newState;
		if (object.isNil) { ^(hasMovedOn: false) };
		
		newState = (
			object: object,
			hasMovedOn: object.hasMovedOn, 
			isCurrent: object.isCurrent,
			started: History.started, 
			
			filtStr: filTextV.string, 
			numLines: object.lines.size
		);
		^newState
	}
	
	resetViews { 
		[startBut, filtBut, keyPop].do(_.value_(0)); 
		textV.string = "";
		filTextV.string = "";
		listV.items = [];	
	}

		// these should move to JITGui in general
	updateFunc { |newState, key, func| 
		var val = newState[key];
		if (val != prevState[key]) { func.value(val) }
	}

	updateVal { |newState, key, guiThing| 
		var val = newState[key];
		if (val != prevState[key]) { guiThing.value_(val) }
	}
	
	updateBinVal { |newState, key, guiThing| 
		var val = newState[key];
		if (val != prevState[key]) { guiThing.value_(val.binaryValue) }
	}
		
	checkUpdate { 
		var newState = this.getState;
		
		if (newState == prevState) { ^this }; 
		
		this.updateFunc(newState, \object, { |obj| zone.enabled_(obj.notNil) });
		
		if (newState[\object].isNil) { 
			this.resetViews;
			prevState = newState; 
			^this 
		};
		
		this.updateFunc(newState, \isCurrent, { |val| startBut.enabled_(val.binaryValue) });
		this.updateBinVal(newState, \started, startBut);
		this.updateBinVal(newState, \filtering, filtBut);
		
			 // clumsy, but filTextV has no usable action...
		if (filTextV.hasFocus and: (newState[\filtStr] != filters[1])) {
			this.setStrFilter(newState[\filtStr]);
		};
		
			// could be factoerd a bit more
		if (newState[\hasMovedOn] or: { newState[\numLines] != prevState[\numLines]}) { 

			var newIndex, selectedLine, linesToShow, keys;
						
			keys = [\all] ++ object.keys.asArray.sort;
			keyPop.items_(keys);
			keyPop.value_(keys.indexOf(filters[0]) ? 0);
			
					// remember old selection
			if (stickMode == 1) {
				selectedLine = (lastLinesShown ? [])[listV.value];
			} { 
					// soemthing else here?
			};

			linesToShow = if (filtering.not) {
				object.lineShorts.array.copy
			} {
				this.filterLines;
				filteredShorts;
			} ? [];

			if (linesToShow != lastLinesShown) {
			//	"or updating listview here?".postln;
				listV.items_(linesToShow);
				lastLinesShown = linesToShow;
			};
			
			newIndex = if (selectedLine.isNil) { 0 }
				{ linesToShow.indexOf(selectedLine) };
			listV.value_(newIndex ? 0);
			if(stickMode == 0) { listV.action.value(listV) };
			object.hasMovedOn = false;
		};

		prevState = newState;
	}

	setKeyFilter { |key| filters.put(0, key); this.filterLines; }
	setStrFilter { |str| filters.put(1, str); this.filterLines; }

	filtering_ { |flag=true|
		 filtering = flag;
		 object.hasMovedOn_(true);
	}
	filterOn { this.filtering_(true) }
	filterOff { this.filtering_(false) }

	filterLines {
		filteredIndices = object.indicesFor(*filters);
		filteredShorts = object.lineShorts[filteredIndices];
		defer {
			keyPop.value_(keyPop.items.indexOf(filters[0] ? 0));
			filTextV.string_(filters[1]);
		};
		if (filtering) { object.hasMovedOn = true; };
	}
	
	postInlined { |index|
		var line;
		if (object.lines.isNil) { "no history lines yet.".postln; ^this };
		line = object.lines[index];
		if (line.isNil) { "history: no line found!".inform; ^this };
		textV.string_(line[2]);
	}
	postDoc { |index|
		var line;
		if (object.lines.isNil) { "no history lines yet.".postln; ^this };
		line = object.lines[index];
		if (line.isNil) { "history: no line found!".inform; ^this };
		this.findDoc;
		doc.string_(line[2]).front;
		try { this.alignDoc };
		w.front;
	}
	alignDoc {
		var docbounds, winbounds;
		docbounds = doc.bounds;
		winbounds = w.bounds;
		doc.bounds_(
			Rect(
				winbounds.left,
				winbounds.top + winbounds.height + 24,
				winbounds.width,
				docHeight
			)
		)
	}
	
	rip { 
		this.findDoc; doc.view.children.first.string_(textV.string); doc.front;
	}
	
	findDoc {
		if (docFlag == \newDoc) { oldDocs = oldDocs.add(doc) };
		if (docFlag == \newDoc or: doc.isNil or: { Window.allWindows.includes(doc).not }) {
			doc = Window(docTitle, Rect(300, 500, 300, 100));
			doc.addFlowLayout;
			TextView(doc, doc.bounds.resizeBy(-8, -8)).resize_(5);
		};
		oldDocs = oldDocs.select {|d| d.notNil and: { d.dataptr.notNil } };
	}
}