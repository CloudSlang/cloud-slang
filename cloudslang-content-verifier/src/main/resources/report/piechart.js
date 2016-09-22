google.charts.load('current', {'packages': ['corechart']});
google.charts.setOnLoadCallback(drawChart);
function drawChart() {
    var passed = parseInt(document.getElementById('passed').innerHTML);
    var failed = parseInt(document.getElementById('failed').innerHTML);
    var skipped = parseInt(document.getElementById('skipped').innerHTML);

    var data = google.visualization.arrayToDataTable([
        ['Header', 'Header'],
        ['Passed', passed],
        ['Failed', failed],
        ['Skipped', skipped]
    ]);

    var options = {
        title: 'Test Case Run Summary',
        is3D: true,
        colors: ['#15DC07', '#DA101D', '#FFA500']
    };

    var chart = new google.visualization.PieChart(document.getElementById('piechart'));
    chart.draw(data, options);
}