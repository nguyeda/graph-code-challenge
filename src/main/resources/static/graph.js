"use strict";

// http://bl.ocks.org/jhb/5955887
// https://bl.ocks.org/shimizu/e6209de87cdddde38dadbb746feaf3a3
// https://bl.ocks.org/pstuffa/3393ff2711a53975040077b7453781a9

var width, height;
var chartWidth, chartHeight;
var margin;
var svg = d3.select("#graph").append("svg");
var chartLayer = svg.append("g").classed("chartLayer", true);
var simulation;

// randomData(4);
//
// function randomData(range) {
//     renderGraph({
//         nodes: d3.range(0, range).map(function (d) {
//             return {name: "node-" + d, numberOfEdges: 0}
//         }),
//         edges: d3.range(0, range).map(function () {
//             return {
//                 source: ~~d3.randomUniform(range)(),
//                 target: ~~d3.randomUniform(range)(),
//                 weight: ~~d3.randomUniform(0, 20)()
//             }
//         })
//     });
// }

function renderGraph(data) {
    console.log('rendering graph from data', data);
    if (simulation) {
        svg.selectAll('*').remove();
        simulation.restart();
    } else {
        simulation = init(data);
    }
    drawChart(data);
}

function init() {
    width = document.querySelector("#graph").clientWidth;
    height = document.querySelector("#graph").clientHeight;

    margin = {top: 0, left: 0, bottom: 0, right: 0};

    chartWidth = width - (margin.left + margin.right);
    chartHeight = height - (margin.top + margin.bottom);

    svg.attr("width", width).attr("height", height);

    chartLayer
        .attr("width", chartWidth)
        .attr("height", chartHeight)
        .attr("transform", "translate(" + [margin.left, margin.top] + ")");

    return d3.forceSimulation()
        .force("link", d3.forceLink().id(function (d) {
            return d.index
        }).distance(function (d) {
            return 50 + d.weight * d.weight
        }))
        .force("collide", d3.forceCollide(function (d) {
            return d.r + 8
        }).iterations(16))
        .force("charge", d3.forceManyBody())
        .force("center", d3.forceCenter(chartWidth / 2, chartHeight / 2))
        .force("y", d3.forceY(0))
        .force("x", d3.forceX(0));
}

function drawChart(data) {
    var link = svg.append("g")
        .attr("class", "edges")
        .selectAll("line")
        .data(data.edges)
        .enter()
        .append("line")
        .attr("stroke", "black");

    var node = svg.append("g")
        .attr("class", "nodes")
        .selectAll("circle")
        .data(data.nodes)
        .enter().append("circle")
        .attr("r", function (d) {
            return 8 + d.numberOfEdges
        })
        .call(d3.drag()
            .on("start", dragstarted)
            .on("drag", dragged)
            .on("end", dragended));


    var ticked = function () {
        link.attr("x1", function (d) {
            return d.source.x;
        })
            .attr("y1", function (d) {
                return d.source.y;
            })
            .attr("x2", function (d) {
                return d.target.x;
            })
            .attr("y2", function (d) {
                return d.target.y;
            });

        node.attr("cx", function (d) {
            return d.x;
        })
            .attr("cy", function (d) {
                return d.y;
            });
    };

    simulation.nodes(data.nodes)
        .on("tick", ticked);

    simulation.force("link")
        .links(data.edges);


    function dragstarted(d) {
        if (!d3.event.active) simulation.alphaTarget(0.3).restart();
        d.fx = d.x;
        d.fy = d.y;
    }

    function dragged(d) {
        d.fx = d3.event.x;
        d.fy = d3.event.y;
    }

    function dragended(d) {
        if (!d3.event.active) simulation.alphaTarget(0);
        d.fx = null;
        d.fy = null;
    }
}