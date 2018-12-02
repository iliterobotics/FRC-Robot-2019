field_width_inches = 27.0 * 12.0 + 72.0
field_height_inches = 27.0 * 12.0

window_width = field_width_inches * 3
window_height = field_height_inches * 3

set term pngcairo background rgb 'white' size window_width,window_height enhanced font "verdana,8"
set multiplot

set xrange [0:field_width_inches]
set yrange [0:field_height_inches]
set xtics 10
set ytics 10

stats 'tracking.csv'
num_records = floor(STATS_records/100.)

plot    'tracking.csv' with lines, \
        'trajectory.csv' with lines

        #, \
        #'tracking.csv' using ((($1 + 19.33) * cos($3 * 3.141 / 180)) - (($2 + 16.955) * sin($3 * 3.141 / 180))):((($1 + 19.33) * sin($3 * 3.141 / 180)) + (($2 + 16.955) * cos($3 * 3.141 / 180)))


plot "field.png" binary filetype=png dx=(field_width_inches / 610.0) dy=(field_height_inches / 514.0) w rgbalpha
