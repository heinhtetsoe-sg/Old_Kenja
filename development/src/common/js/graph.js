
function createRadarChart(type, cntno) {

    var create = type;
    var stack  = false;
    if(type == "stacked"){
        create = "bar";
        stack = true;
    }

    //y軸の最大値と最小値をどうするか
    var beginatzero = false;
    var max = document.forms[0]["ymax"+cntno].value;
    var min = document.forms[0]["ymin"+cntno].value;
    var maxTicksLimit = document.forms[0].maxTicksLimit.value;

    if(type != "radar" ){
        var ticks = "{ beginAtZero:"+beginatzero+", max:"+ max +", min:"+ min +", maxTicksLimit: "+ maxTicksLimit +",}";
        ticks = (new Function("return " + ticks))();    //objectに変換
        var scal = "{ yAxes: [{ stacked:"+ stack+",}], xAxes: [{stacked:"+ stack+"}]}";
        var scales = (new Function("return " + scal))();    //objectに変換
        scales["yAxes"][0]["ticks"] = ticks;
        
    }else{
        var scal = "{reverse: false,ticks: {beginAtZero:"+beginatzero+", max:"+ max +", min:"+ min +", maxTicksLimit: "+ maxTicksLimit +",}}";
        var scales = (new Function("return " + scal))();     //objectに変換
    }

    var option = "{animation: false,}";
    option = (new Function("return " + option))();     //objectに変換
    if(type != "radar"){
        option["scales"] = scales;
    }else{
        option["scale"] = scales;
    }
    
    //tooltip
    option["tooltips"] = { mode: 'label', }
    
    //使用するcanvas
    document.getElementById(create+cntno).style.display="block"
    
    //タイトル
    var title = document.forms[0]["title"+cntno].value;
    if(title != ''){
        option["title"] = {
                display: true,
                text: title
            }
    }else{
        option["title"] = {
                display: false,
            }
    }
    //Label
    var setLabelSet = getLabelSetRadar(type, cntno);
    //data
    var setDataSet = getDataSetRadar(type, cntno);
    

    var ctx = document.getElementById(create+cntno);

    var extra = {   type: create,
                    data: {
                           labels: setLabelSet,
                           datasets: setDataSet,
                          },
                    options: option
                }
        var myRadar = new Chart(ctx, extra);
        

    return false;
}

//Labelデータ取得・作成
function getLabelSetRadar(type,cntno) {
    var hogeLabel = document.forms[0]["hogeLabel"+cntno].value;
    var retArray = hogeLabel.split(",");

    return retArray;
}

//dataデータ取得・作成
function getDataSetRadar(type,cntno) {
    //凡例数
    var cnt = document.forms[0]["hogeCnt"+cntno].value;
    //データ
    var dataArray = new Array();
    for (var i = 0; i < cnt; i++) {
        var hogeval = document.forms[0]["hogeval"+cntno + i].value;
        var hogeSet = hogeval.split(",");
        dataArray[i] = hogeSet;
    }
    //凡例
    var hogeHanreiVal = document.forms[0]["hogeHanrei"+cntno].value;
    var hogeHanreiSet = hogeHanreiVal.split(",");

    //表示データのタイプ
    var hogeTypeVal = document.forms[0]["hogeType"+cntno].value;
    var hogeTypeSet = hogeTypeVal.split(",");

    //表示データの線の種類
    var hogeLineTypeVal = document.forms[0]["hogeLineType"+cntno].value;
    var hogeLineTypeSet = hogeLineTypeVal.split(",");
    
    //線の種類変更用配列
    var dash = new Array();
    dash[0] = [0,10];    //線無し
    dash[1] = [0,0];     //実線
    dash[2] = [5,5];     //点線

    //色
    var hogeColorVal = document.forms[0]["hogeColor"+cntno].value;
    var hogeColorSet = hogeColorVal.split(",");

    var retArray = new Array();
    for (var i = 0; i < cnt; i++) {
        if(hogeColorVal != ""){
            var Color = hogeColorSet[i];
                Color = Color.replace("/", ",");    //255,255/255になる
                Color = Color.replace("/", ",");    //255,255,255になる

            var setRgba = "rgba("+Color+",0.5)";
            var setRgba2 = "rgba("+Color+",1)";
        }else{
            if(i>19){   //20個以上になったら色をランダムにする
                var setRgba = 'rgba(' + randomColorFactor() + ',' + randomColorFactor() + ',' + randomColorFactor() + ', 0.5)';
                var setRgba2 = setRgba.replace('0.5','1');
            }else{      //20個までは指定した10色を使用
                var setRgba = getRgba(i);
                var setRgba2 = getRgba2(i);
            }
        }
        
        //文字を数字に変換したい
        var dataInt = dataArray[i];
        var arraycnt = dataInt.length;
        var dataIntAll = new Array();
        for(j=0;j<arraycnt;j++){
            dataIntAll[j] = parseFloat(dataInt[j],10);
        }
        
        if(type == "radar"){
            var fill = true;
        }else{
            var fill = false;
        }
        //datasetsの中身
        retArray[i] = 
                    {
                      type: hogeTypeSet[i],
                      tension: 0,       //0だと直線
                      data : dataIntAll,
                      label : hogeHanreiSet[i],
                      borderWidth: 2,
                      borderColor: setRgba2,
                      backgroundColor: setRgba,
                      fill: fill,
                      pointBackgroundColor: setRgba2,
                      pointBorderColor: "#fff",
                      pointRadius: 5,
                      pointHoverRadius: 7,
                      pointHitRadius: 15,
                      borderDash: dash[hogeLineTypeSet[i]],
                    };
    }
    return retArray;
}

function getRgba(cnt) {
    var retVal = "rgba(220,220,220,0.5)";
    if(cnt > 9){
        var amari = cnt -10;
    }else{
        var amari = cnt;
    }
    switch (amari){
      case 0:
        retVal = "rgba(255,150,0,0.5)";     //オレンジ
        break;
      case 1:
        retVal = "rgba(220,20,60,0.5)";     //赤
        break;
      case 2:
        retVal = "rgba(65,105,225,0.5)";   //青
        break;
      case 3:
        retVal = "rgba(0,143,35,0.5)";   //緑
        break;
      case 4:
        retVal = "rgba(138,43,226,0.5)";   //赤紫
        break;
      case 5:
        retVal = "rgba(250,10,250,0.5)";     //ピンク
        break;
      case 6:
        retVal = "rgba(0,173,234,0.5)";     //水色
        break;
      case 7:
        retVal = "rgba(0,10,115,0.5)";   //紺
        break;
      case 8:
        retVal = "rgba(158,199,0,0.5)";   //黄緑
        break;
      case 9:
        retVal = "rgba(98,45,24,0.5)";   //セピア
        break;
    }
    return retVal;
}

function getRgba2(cnt) {
    var retVal = "rgba(220,220,220,1)";
    if(cnt > 9){
        var amari = cnt -10;
    }else{
        var amari = cnt;
    }
    switch (amari){
      case 0:
        retVal = "rgba(255,150,0,1)";     //オレンジ
        break;
      case 1:
        retVal = "rgba(220,20,60,1)";     //赤
        break;
      case 2:
        retVal = "rgba(65,105,225,1)";   //青
        break;
      case 3:
        retVal = "rgba(0,143,35,1)";   //緑
        break;
      case 4:
        retVal = "rgba(138,43,226,1)";   //赤紫
        break;
      case 5:
        retVal = "rgba(250,10,250,1)";     //ピンク
        break;
      case 6:
        retVal = "rgba(0,173,234,1)";     //水色
        break;
      case 7:
        retVal = "rgba(0,10,115,1)";   //紺
        break;
      case 8:
        retVal = "rgba(158,199,0,1)";   //黄緑
        break;
      case 9:
        retVal = "rgba(98,45,24,1)";   //セピア
        break;
    }
    return retVal;
}

var randomColorFactor = function() {
    return Math.round(Math.random() * 255);
};

