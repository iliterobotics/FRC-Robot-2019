set key autotitle columnhead
set datafile separator ","
set xrange [-200:200]
set yrange [-200:200]

plot 'debug.csv' using 6:7, \
	 'debug.csv' using 8:9