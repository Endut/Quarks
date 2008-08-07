//redFrik - released under gnu gpl license

//--changes 080807:
//fixed serious bug when 3rd party classes not installed
//fixed duration overlap bug.  added stopAheadTime
//made into a quark
//now works with more items (NodeProxy, BBCut2, RedMOD, RedXM, Function, SynthDef)
//quite a few internal changes - no big changes to the interface
//--071102:
//first release

//--todo:
//empty section example (index 0, 1, 2, 5)
//RedSeq.  also extra gui class with esc key for next, section data, playung tracks etc
//special redtrk subclass that can force stop for long duration events. using Pbus?

//--notes:
//redxm and redmod can change redmst clock tempo!
//events with long duration does not get cut off
//don't use CutBuf1 as it will not stop correctly.  replace with CutBuf2

RedMst {
	classvar	<tracks, <>clock, <>quant= 4,
			<section= 0, <maxSection= 0,
			<>stopAheadTime= 0.05,
			alreadyJumping= false;
	*initClass {
		tracks= ();
		clock= TempoClock.default;
	}
	*at {|key|
		^tracks[key];
	}
	*add {|trk|
		tracks.put(trk.key, trk);
		if(trk.sections.maxItem>maxSection, {
			maxSection= trk.sections.maxItem;
		});
	}
	*remove {|trk|
		trk.stop;
		tracks.put(trk.key, nil);
	}
	*clear {
		tracks.do{|x| x.clear};
		tracks= ();
		section= 0;
		maxSection= 0;
		if(clock!=TempoClock.default, {
			clock.stop;
			clock.clear;
			clock= TempoClock.default;
		});
	}
	*stop {
		if(clock.notNil, {
			clock.schedAbs(clock.nextTimeOnGrid(quant)-stopAheadTime, {
				tracks.do{|x| x.stop};
				nil;
			});
		}, {
			"RedMst: clock is nil - stopping now".warn;
			tracks.do{|x| x.stop};
		});
	}
	*play {|startSection= 0|
		this.goto(startSection);
	}
	*goto {|jumpSection|
		if(clock.isNil, {
			clock= TempoClock.default;
			"RedMst: clock is nil - using TempoClock.default".warn;
		});
		if(alreadyJumping, {
			"RedMst: already jumping somewhere - goto ignored".warn;
		}, {
			clock.schedAbs(clock.nextTimeOnGrid(quant)-stopAheadTime, {
				tracks.do{|x|
					if(x.sections.includes(jumpSection).not and:{x.isPlaying}, {
						x.stop;
					});
				};
				nil;
			});
			clock.schedAbs(clock.nextTimeOnGrid(quant), {
				section= jumpSection;
				if(section>maxSection, {
					"RedMst: reached the end".postln;
				}, {
					("RedMst: new section:"+section+"of"+maxSection).postln;
				});
				tracks.do{|x|
					if(x.sections.includes(section) and:{x.isPlaying.not}, {
						x.play;
					});
				};
				alreadyJumping= false;
				nil;
			});
			alreadyJumping= true;
		});
	}
	*next {
		this.goto(section+1);
	}
	*prev {
		this.goto(section-1);
	}
}
