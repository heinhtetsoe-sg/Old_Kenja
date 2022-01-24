window.onload = function(){
    var cmd = document.forms[0].cmd.value;
    var gradio = document.forms[0].gradio.value;
    var radio = document.forms[0].radio.value;
    if(cmd == "graph" || cmd == "" || cmd == "yearChange"){
        if(radio != 4 && gradio != "1"){
            var cnt = document.forms[0].GRAPH.value;
        }
        if(gradio != "1"){
            if(radio != 4){
                for(graph=0;graph<cnt;graph++){
                    //グラフ作成
                    document.getElementById("bar"+graph).style.display="none";
                    createRadarChart("bar", graph);
                }
            }else{
                //グラフ作成
                document.getElementById("bar0").style.display="none";
                createRadarChart("bar", 0);
            }
        }else{
            //グラフ作成
            document.getElementById("radar0").style.display="none";
            createRadarChart("radar", 0);
        }
    }

}

function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    
    return false;
}

//オールチェック
function allCheck(div, obj) {
    var checked = "";
    if (obj.checked) {
        checked = "checked";
    }
    checkedSet(0, document.forms[0].CHK_TEST_CNT.value, "CHK_TEST", checked);
}

//Checkedにする
function checkedSet(chkcnt, cnt, elemchk, checkval) {
    for (; chkcnt < cnt; chkcnt++) {
        document.getElementById(elemchk + chkcnt).checked = checkval;
    }
}

//グラフへデータを渡す
function addDataToAppletCheck(url, cmd, year, semester) {
    var paraData = "";  //POST：グラフデータ
    var comboIndex = 0; //POST：グラフ種別を送信 0:折線 1:レーダー 2:棒 3:折線＋棒
    if ("radar" == cmd) {
        comboIndex = 1;
        paraData  = addData(0, document.forms[0].CHK_TEST_CNT.value, "CHK_TEST", "DEVIATION");
    } else {
        paraData  = addData(0, document.forms[0].CHK_TEST_CNT.value, "CHK_TEST", "SCORE");
    }
    paraData = paraData.substr(0, paraData.length - 1);

    document.graph_applet.adpara.value        = paraData;   //グラフデータ
    document.graph_applet.cmbIndex.value      = comboIndex; //グラフ種別
    document.graph_applet.year.value          = year;       //年度
    document.graph_applet.semester.value      = semester;   //学期
    document.graph_applet.schregno.value      = document.forms[0].SCHREGNO.value;
    document.graph_applet.action = url;
    document.graph_applet.submit();
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

//グラフ種類変えたとき平均なしをdisabledにするか
function radio_change(radio) {
    if(radio.value == '1'){
        document.getElementById("RADIO4").disabled = true;
    }else{
        document.getElementById("RADIO4").disabled = false;
    }
}

