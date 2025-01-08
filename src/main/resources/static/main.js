// Copyright 2022 Takanori Fujiwara.
// Released under the BSD 3-Clause 'New' or 'Revised' License

import {
  boxPlot
} from './chart.js';

//const diamonds = await d3.csv('./data/diamonds.csv', d3.autoType);
const diamonds = [
{carat:0.23,price:326},
{carat:0.21,price:326},
{carat:0.23,price:327},
{carat:0.29,price:334},
{carat:0.31,price:335},
{carat:0.24,price:336},
{carat:0.24,price:336},
{carat:0.26,price:337},
{carat:0.22,price:337},
{carat:0.23,price:338},
{carat:0.3,price:339},
{carat:0.23,price:340},
{carat:0.22,price:342},
{carat:0.31,price:344},
{carat:0.2,price:345},
{carat:0.32,price:345},
{carat:0.3,price:348},
{carat:0.3,price:351},
{carat:0.3,price:351},
{carat:0.3,price:351},
{carat:0.3,price:351},
{carat:0.23,price:352},
{carat:0.23,price:353},
{carat:0.31,price:353},
{carat:0.31,price:353}
]

const chart = boxPlot(diamonds, {
  x: d => d.carat,
  y: d => d.price,
  xLabel: 'Carats →',
  yLabel: '↑ Price ($)',
  width: 1000,
  height: 500
});

d3.select('body').append(() => chart);