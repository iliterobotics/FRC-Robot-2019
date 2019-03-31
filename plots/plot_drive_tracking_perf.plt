set key autotitle columnhead
set datafile separator ","
set terminal wxt size 800,800

set xrange [0:(27 * 12)]
set yrange [0:-(27 * 12)]

plot 'debug.csv' using 6:(last_x = $7), \
	 'debug.csv' using 8:(last_y = $9)
