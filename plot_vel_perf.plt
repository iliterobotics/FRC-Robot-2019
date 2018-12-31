set key autotitle columnhead
set datafile separator ","

set yrange [-120:120]

lastTime = 0.0
plot 'debug.csv' using 1:2 with linespoints, '' using 1:3 with linespoints, '' using 1:4 with linespoints, '' using 1:5 with linespoints, '' using 1:10 with linespoints, '' using 1:11 with linespoints