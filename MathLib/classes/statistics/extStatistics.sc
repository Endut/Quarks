// some statistics, adc 2005, plus ds 2007.

+ Collection {
			// same as sum, but sum initialized as float 0.0 to avoid numerical
			// errors for sums of large integer arrays.
	sumF { | function |			
		var sum = 0.0;			
		if (function.isNil) { 		// optimized version if no function
			this.do { | elem | sum = sum + elem; }
		}{
			this.do {|elem, i| sum = sum + function.value(elem, i); }
		};
		^sum
	}
	
	meanF { ^this.sumF / this.size }
	
	geoMean { 
		// 	this.product ** (this.size.reciprocal);	// fails for big arrays, 
		^2 ** this.mean({ |el| el.log2 })			// log2 method is slower but safer.
	}
	
	harmMean { 
		^this.size / this.sumF({ |el| el.reciprocal })
	}
	
	variance { arg mean;	// supply mean if known
		mean = mean ?? { this.meanF };
		
		^(this.sumF { |el| (el - mean).abs.squared } / (this.size - 1).max(1))
	}
	
	stdDev { arg mean; ^this.variance(mean).sqrt; }
	
	skew { arg mean;	// supply mean if known
		mean = mean ?? { this.meanF };		
	
		^this.sumF({ |el| (el - mean).cubed }) / (this.size * this.stdDev(mean).cubed)
	}
	
		// kurtosis is tails size : kurtosis > 0 is leptokurtic, i.e. large tails; 
		// 0 is normal distribution, < 1 is platykurtic = small tails.
		// not quite sure how to test that this formula is correct. 
	kurtosis { arg mean;	// supply mean if known
		mean = mean ?? { this.meanF };		
		
		^(this.sumF({ |el| (el - mean).squared.squared })
		/ ((this.size - 1)  * this.variance.squared)) - 3
	}
		
						// standard normal distribution form:
						// avg = 0, stdDev = 1.
	zTable { arg mean, stdDev;	// supply mean and stdDev if you can
						// compact formula is:
	//	^this - this.meanF / this.stdDev; 

		mean = mean ?? { this.meanF };
		stdDev = stdDev ?? { this.stdDev(mean) };	
		^this - mean / stdDev
	}
	
					// typically assumes this is sorted! 
	atPercent { |index=0.5, interpol=true| 
		if (interpol) 
			{ ^this.blendAt(index * (this.size - 1)) }
			{ ^this.at( index * (this.size - 1).round.asInteger) }
	}

	/* naive method
	percentile { |percent=#[0.25, 0.5, 0.75], interpol=true| 
		^this.copy.sort.atPercent(percent, interpol)
	}
	*/
	// Faster method
	percentile { |percent=#[0.25, 0.5, 0.75], interpol=true|
		var sorted, index, indexint;
		sorted = this.copy;
		// We don't actually sort the entire array since that may be expensive for large arrays.
		// Instead we use Hoare's partitioning method to sort it "just enough"
		percent.asArray.do{|pc|
			index = pc * (this.size - 1);
			indexint = index.floor.asInteger;
			if(index == indexint){
				sorted.hoareFind(indexint);
			}{
				sorted.hoareFind(indexint);
				sorted.hoareFind(indexint + 1);
			};
		};
		^sorted.atPercent(percent, interpol)
	}
			// median exists already, median2 interpolates.
	median2 {
		^this.percentile(0.5, true);
	}
	
	trimedian { |interpol=true|
		^this.percentile([0.25, 0.5, 0.5, 0.75], interpol).meanF;
	}
}


+ SequenceableCollection { 
			// Pearson correlation.
	corr { arg that; 
		var num, denom, thisSum, thatSum; 
		
		if (this.size != that.size, { 
			"No correlation between colls of unequal size.".error; 
			^nil 
		}); 
		 
		thisSum = this.sumF;
		thatSum = that.sumF;
		
		num = this.sumF({ |el, i| el * that[i] }) - (thisSum * thatSum / this.size); 
		
		denom = sqrt( 
			(this.sumF({ |el| el.squared }) - (thisSum.squared / this.size)) 
			* (that.sumF({ |el| el.squared }) - (thatSum.squared / that.size))
		);
		^num / denom
	}
	
	// Order-Statistics Correlation Coefficient
	// see Xu et al 2007, DOI 10.1109/TSP.2007.899374
/*
x = (0 .. 100);
y = (0 .. 100);
oscorr(x,y);
x = (0 .. 100);
y = (100 .. 0);
oscorr(x,y);
x = (0 .. 100);
y = 100.dup(100) ++ 5;
oscorr(x,y); // Strange result here! Perfect correlation, despite drastic difference
x = (0 .. 100);
y = 100.dup(98) ++ [5, 50, -50]; // Hmm
oscorr(x,y);
x = {100.rand}.dup(101)
y = x
y = x + {100.rand}.dup(101)
y = x + {1000.rand}.dup(101)
oscorr(x,y);
*/
	oscorr { |that|
		// "this" is x, "that" is y
		var xsorted, ysorted, yconcomitant, last;
		
		// Compiles x and y into vector values, then sorts purely using x values
		#xsorted, yconcomitant = this.collect{|xitem, index| [xitem, that[index]]}.sort{ |a, b| a[0] <= b[0] }.flop;
		// Sorts y, we don't need to know the concomitant x values for the calculation
		ysorted = that.sort;
		
		last = this.lastIndex;
		
		^
			(this.sumF{|xi, i| (xi - this[last - i]) * yconcomitant[i] })
				/
			(this.sumF{|xi, i| (xi - this[last - i]) * ysorted[i] })
	}
	
		// return n sorted indices and values for a given sort function
	nSorted { |n, func| 
		var sorted, indexedArr;
		func = func ? { arg a, b; a < b }; 
		n = n ? this.size; 
		
		sorted = SortedList(n, { |a, b| func.value(a[1], b[1]) }); 
		
		this.do { |el, i| sorted.add([i, el]); 
			if(sorted.size > n) { sorted.removeAt(n) } 
		};
		^sorted.array
	}
	
	// Weighted mean and variance of a list of values.
	// To estimate mean and variance from a histogram:
	//    this = bin centres, weights = bin frequencies (heights)
	wmean { |weights|
		^this.sum{|val, index| val * weights[index]} / weights.sum;
	}
	wvariance { |weights, wmean|
		if(wmean.isNil, {wmean = this.wmean(weights)});
		^this.sum{|val, index| (val - wmean) * (val - wmean) * weights[index]} / weights.sum;
	}
	
	// Normalised autocorrelation, calculated for lags of zero up to "num"
	autocorr { |num, mean, sd|
		var data, sum, n;
		n = this.size;
		num  = num  ?? { n - 1 };
		mean = mean ?? { this.mean };
		sd   = sd   ?? { this.stdDev(mean) };
		
		data = this - mean;
		
		^num.collect{ |k|
			sum = 0;
			(0 .. n-k-1).do{ |t|
				sum = sum + (data[t] * data[t+k]);
			};
			sum = sum / ((n-k) * sd);
		};
	}
	
}
