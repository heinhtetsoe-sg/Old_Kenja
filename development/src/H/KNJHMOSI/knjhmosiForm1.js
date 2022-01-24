
function createChart(type) {
    document.getElementById("line").style.display="none"
    document.getElementById("bezi").style.display="none"
    document.getElementById("bar").style.display="none"
    document.getElementById("radar").style.display="none"
    document.getElementById("chart_legend").style.display="none"

    document.getElementById("hoge").style.display="block"

    var setDataSet = getDataSetBarLine();

    var chartdata46 = {

      "config": {
        "title": "Option minY",
        "subTitle": "Y軸の最小値を指定します。指定しなければデータの最小値がminYになります。",
        "maxY": 100,
        "minY": 0,
        "useVal": "yes",
        "type": type  //bezi ベジェ指定可
      },

      "data": setDataSet
    };
    ccchart.init("hoge", chartdata46)
    return false;
}

function createChart2() {
    document.getElementById("hoge").style.display="block"
    document.getElementById("radar").style.display="none"
    document.getElementById("chart_legend").style.display="none"

    var setDataSet = getDataSetBarLine();
    var chartdata73 = {

      "config": {
        "title": "第○回　共通テスト",
        "subTitle": "駿台テスト",
        "maxY": 100,
        "minY": 0,
        "type": "bar",
        "useVal": "yes"
      },

      "data": setDataSet
    };

    ccchart.init('hoge', chartdata73);
    return false;
}

function getDataSetBarLine() {
    var dataArray = new Array();
    var cnt = document.forms[0].cccCnt.value;
    for (var i = 0; i < cnt; i++) {
        var hogeval = document.forms[0]["cccVal" + i].value;
        var hogeSet = hogeval.split(",");
        dataArray[i] = hogeSet;
    }

    var retArray = new Array();
    for (var i = 0; i < cnt; i++) {
        retArray[i] = dataArray[i];
    }
    return retArray;
}

function createRadarChart(type) {
    document.getElementById("hoge").style.display="none"
    document.getElementById("line").style.display="none"
    document.getElementById("bezi").style.display="none"
    document.getElementById("bar").style.display="none"
    document.getElementById("radar").style.display="none"
    document.getElementById("chart_legend").style.display="block"
    document.getElementById(type).style.display="block"

    var setLabelSet = getLabelSetRadar();
    var setDataSet = getDataSetRadar();
    var radarChartData = {
      labels : setLabelSet,
      datasets : setDataSet
    }

    var beziVal = false;
    if (type == "bezi") {
        beziVal = true;
    }

    var animationVal = document.forms[0].ANIME.checked;
    var option = {
        scaleLineWidth : 1,         // 区切りの太さ(px)
        scaleOverride: true,        // 区切りを絶対値で指定
        scaleSteps : 10,            // 区切りの数
        scaleStepWidth : 10,        // 区切りの間隔(100％がMAXのグラフなら、100/scaleSteps)
        scaleStartValue : 0,        // 区切りの開始値(100%がMAXのグラフなら、0％の0)
        scaleShowLabels : true,     // 区切りのラベル
        scaleFontSize : 10,         // 区切りのラベルサイズ
        pointLabelFontSize : 15,    // ラベルのサイズ
        datasetFill : false,        // レーダー内の塗りつぶし
        scaleGridLineColor : "rgba(10,200,200,1.0)",
        scaleLineColor : "rgba(10,200,200,1.0)",
        datasetStrokeWidth : 3,
        animation : animationVal,   //アニメーション
        backgroundColor : "gray",
        bezierCurve : beziVal
    }
    if (type == "line" || type == "bezi") {
        var myRadar = new Chart(document.getElementById(type).getContext("2d")).Line(radarChartData, option);
    } else if (type == "bar") {
        var myRadar = new Chart(document.getElementById(type).getContext("2d")).Bar(radarChartData, option);
    } else {
        var myRadar = new Chart(document.getElementById(type).getContext("2d")).Radar(radarChartData, option);
    }
//    document.getElementById("chart_legend").innerHTML = myRadar.generateLegend();
    var cnt = document.forms[0].hogeCnt.value;
    var hogeHanreiVal = document.forms[0]["hogeHanrei"].value;
    var hogeHanreiSet = hogeHanreiVal.split(",");
    var setUl = "";
    for (var i = 0; i < cnt; i++) {
        var setRgba2 = getRgba2(i);
        setUl = setUl + "<span style=\"background-color:" + setRgba2 + "\">　</span>" + hogeHanreiSet[i] + "<BR>";
    }
    document.getElementById("chart_legend").innerHTML = setUl;

    return false;
}

function getLabelSetRadar() {
    var hogeLabel = document.forms[0]["hogeLabel"].value;
    var retArray = hogeLabel.split(",");

    return retArray;
}

function getDataSetRadar() {
    var cnt = document.forms[0].hogeCnt.value;
    var dataArray = new Array();
    for (var i = 0; i < cnt; i++) {
        var hogeval = document.forms[0]["hogeval" + i].value;
        var hogeSet = hogeval.split(",");
        dataArray[i] = hogeSet;
    }
    var hogeHanreiVal = document.forms[0]["hogeHanrei"].value;
    var hogeHanreiSet = hogeHanreiVal.split(",");

    var retArray = new Array();
    for (var i = 0; i < cnt; i++) {
        var setRgba = getRgba(i);
        var setRgba2 = getRgba2(i);
        retArray[i] = 
                    {
                      fillColor : setRgba,
                      strokeColor : setRgba2,
                      pointColor : setRgba2,
                      pointStrokeColor : "#fff",
                      data : dataArray[i],
                      label : hogeHanreiSet[i]
                    };
    }
    return retArray;
}

function getRgba(cnt) {
    var retVal = "rgba(220,220,220,0.5)";
    switch (cnt){
      case 0:
        retVal = "rgba(255,150,0,0.5)";
        break;
      case 1:
        retVal = "rgba(255,20,80,0.5)";
        break;
      case 2:
        retVal = "rgba(100,150,190,0.5)";
        break;
      case 3:
        retVal = "rgba(220,220,220,0.5)";
        break;
      case 4:
        retVal = "rgba(220,220,220,0.5)";
        break;
    }
    return retVal;
}

function getRgba2(cnt) {
    var retVal = "rgba(220,220,220,1)";
    switch (cnt){
      case 0:
        retVal = "rgba(255,150,0,1)";
        break;
      case 1:
        retVal = "rgba(255,0,0,1)";
        break;
      case 2:
        retVal = "rgba(100,150,190,1)";
        break;
      case 3:
        retVal = "rgba(220,220,220,1)";
        break;
      case 4:
        retVal = "rgba(220,220,220,1)";
        break;
    }
    return retVal;
}

function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//グラフデータセット
function addData(chkcnt, cnt, elemchk, elemhid) {
    var rtnData = "";
    var com = ",";
    for (; chkcnt < cnt; chkcnt++) {
        if (document.getElementById(elemchk + chkcnt).checked) {
            rtnData += document.forms[0][elemhid + chkcnt].value + com;
        }
    }
    return rtnData;
}
