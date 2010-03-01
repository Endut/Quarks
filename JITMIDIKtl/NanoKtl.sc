
NanoKtl : MIDIKtl { 
	
		ctlNames = defaults[this.class];
		lastVals = ();

		pxmixers = ();

		pxOffsets = (1: 0, 2: 0, 3: 0, 4: 0);
		parOffsets = (1: 0, 2: 0, 3: 0, 4: 0);
		
		 	
					var parKey =  pxEditors[scene].editKeys[i + parOffsets[scene]];
					var normVal = val / 127;
					var lastVal = lastVals[key];
					if (parKey.notNil and: proxy.notNil) { 
						proxy.softSet(parKey, normVal, softWithin, lastVal: lastVal) 
					};
					lastVals.put(key, normVal);
			var lastVal = lastVals[\kn9];
			var mappedVol = \amp.asSpec.map(val / 127);
			var proxy = pxEditors[scene].proxy;
			if (lastVal.notNil) { lastVal = \amp.asSpec.map(lastVal) };
				proxy.softVol_(mappedVol, softWithin, pause: volPause, lastVal: lastVal) 
			};
			lastVals[\kn9] = mappedVol;

			// can't softVol server volume ... hmm. do it by hand? 
		
					var lastVal = lastVals[key]; 
					var mappedVal = \amp.asSpec.map(val / 127); 
					
					var lastVol = if (lastVal.notNil) { \amp.asSpec.map(lastVal) }; 
					try { 
				//		"/// *** softVol_: ".post;
						pxmixers[scene].pxMons[i + pxOffsets[scene]].proxy
					lastVals[key] =  mappedVal;
		
				sl1: '0_2',  sl2: '0_3',  sl3: '0_4',  sl4: '0_5',  sl5: '0_6',  sl6: '0_8',  sl7: '0_9',  sl8: '0_12', sl9: '0_13',
				bu1: '0_23', bu2: '0_24', bu3: '0_25', bu4: '0_26', bu5: '0_27', bu6: '0_28', bu7: '0_29', bu8: '0_30', bu9: '0_31',
				bd1: '0_33', bd2: '0_34', bd3: '0_35', bd4: '0_36', bd5: '0_37', bd6: '0_38', bd7: '0_39', bd8: '0_40', bd9: '0_41'
			), 
				mode: 'push', 
				kn1: '0_57', kn2: '0_58', kn3: '0_59', kn4: '0_60', kn5: '0_61', kn6: '0_62', kn7: '0_63', kn8: '0_65', kn9: '0_66',
				sl1: '0_42', sl2: '0_43', sl3: '0_50', sl4: '0_51', sl5: '0_52', sl6: '0_53', sl7: '0_54', sl8: '0_55', sl9: '0_56',
				mode: 'push',	
				kn1: '0_94',  kn2: '0_95', kn3:  '0_96',  kn4: '0_97',  kn5: '0_102', kn6: '0_103', kn7: '0_104', kn8: '0_105', kn9: '0_106',
				sl1: '0_85',  sl2: '0_86', sl3:  '0_87',  sl4: '0_88',  sl5: '0_89',  sl6: '0_90',  sl7: '0_91', sl8:  '0_92',  sl9: '0_93',
				mode: 'toggle', 
				kn1: '0_10', kn2: '1_10', kn3: '2_10', kn4: '3_10', kn5: '4_10', kn6: '5_10', kn7: '6_10', kn8: '7_10', kn9: '8_10',				sl1: '0_7',  sl2: '1_7',  sl3: '2_7',  sl4: '3_7',  sl5: '4_7',  sl6: '5_7',  sl7: '6_7',  sl8: '7_7',  sl9: '8_7',
				bu1: '0_16', bu2: '1_16', bu3: '2_16', bu4: '3_16', bu5: '4_16', bu6: '5_16', bu7: '6_16', bu8: '7_16', bu9: '8_16',