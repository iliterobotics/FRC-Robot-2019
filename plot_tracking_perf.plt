set key autotitle columnhead
set datafile separator ","
set terminal wxt size 800,800

set xrange [-200:200]
set yrange [-200:200]

plot 'debug.csv' using 6:(last_x = $7), \
	 'debug.csv' using 8:(last_y = $9)