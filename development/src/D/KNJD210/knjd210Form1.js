function getGtreData(gtredata){
    document.forms[0].GTREDATA.value = gtredata;
    document.forms[0].cmd.value = 'main';
    document.forms[0].submit();
    return false;
}
function btn_submit(cmd) {
    if (cmd == 'cancel' && !confirm('{rval MSG106}')){
        return true;
    }else if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return true;
    }else if (cmd == 'delete'){
        var chk = document.all['CHECKED[]'];
        for (var i=0;i<chk.length;i++)
        {
            if (chk[i].checked) break;
        }
        if (i == chk.length){
            alert("チェックボックスが選択されていません。");
            return true;
        }
    }else if (cmd == 'avg'){ //平均点補正処理
        loadwindow('knjd210index.php?cmd=avg',0,0,550,200);
        return true;
    }else if (cmd == 'assess'){ //相対評価処理
        loadwindow('knjd210index.php?cmd=assess',0,0,450,250);
        return true;
    }else if (cmd == 'update'){
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function calc(obj){
    var sum1 = sum2 = sum3 = 0;
    //数字チェック
    if (isNaN(obj.value)){
        alert('{rval MSG907}');
        obj.value = obj.defaultValue;
        return;
    }
    //最大値チェック
    if (obj.name == "VALUATION[]" && obj.value != '' && !isNaN(obj.value) && parseInt(obj.value,10) > parseInt(document.forms[0].ASSESSLEVEL.value,10)){
        alert('{rval MSG914}');
        obj.value = obj.defaultValue;
        return;
    }
    var VAL = document.all['VALUATION[]'];
    var G_C = document.all['GET_CREDIT[]'];
    var A_C = document.all['ADD_CREDIT[]'];
    for (var i = 0;i < VAL.length; i++)
    {
        if (VAL[i].value != '' && !isNaN(VAL[i].value)){
            sum1 += parseInt(VAL[i].value,10);
        }
        if (G_C[i].value != '' && !isNaN(G_C[i].value)){
            sum2 += parseInt(G_C[i].value,10);
        }
        if (A_C[i].value != '' && !isNaN(A_C[i].value)){
            sum3 += parseInt(A_C[i].value,10);
        }
        if (obj == VAL[i] || obj == G_C[i] || obj == A_C[i]){
            var idx = i;
        }
    }
    if (VAL.length && VAL.length != 0){
        
        sum1 = Math.round((sum1/VAL.length)*10)/10.0;
        sum2 = Math.round((sum2/VAL.length)*10)/10.0;
        sum3 = Math.round((sum3/VAL.length)*10)/10.0;
        
        outputLAYER('AVG_VALUATION' , sum1);
        outputLAYER('AVG_GET_CREDIT' ,sum2);
        outputLAYER('AVG_ADD_CREDIT' ,sum3);
    } else {
        outputLAYER('AVG_VALUATION' , VAL.value);
        outputLAYER('AVG_GET_CREDIT' ,G_C.value);
        outputLAYER('AVG_ADD_CREDIT' ,A_C.value);    
    }
}
function check_all(obj){
    for (var i=0; i < document.forms[0].elements.length; i++)
    {
        if (document.forms[0].elements[i].name == "CHECKED[]"){
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}
// /*
//    @(f)
// 
//     機能      : 平均点補正処理
// 
//     返り値    : なし
// 
//     引き数    : ARG1 - 現在の平均点
// 
//     機能説明  : 平均点補正処理を実施する。
// 
//     備考        h:現在の平均点
//                 s:(60-h)73           '98/09/03修正（60点は以前の基準値なのでG_平均点補正係数として可変にした）
//                 y:修正点
//                 x:生徒毎の仮評価
//                 n:修正後平均点
// */
function calcAvgRevise(avg1, avg2, avg3){
    var Wk_Param = Math.round((avg3 - avg1) / 3);
    var obj = document.all['VALUATION\[\]'];
    //var obj = document.forms[0]['ASSESSLEVEL\[\]'];
    var sum = 0;
    for (var i = 0; i < obj.length; i++)
    {
        if (obj[i].value != '' && !isNaN(obj[i].value)){
            var Wk_Score = parseInt(obj[i].value,10);
            if(avg1 < avg3){    //現在の平均点が60未満のとき
                //仮評価が現在の平均点未満のとき (公式１） y = (n - s) 7 ｈ  X ＋ S
                if(Wk_Score < avg1){
                    Wk_Score = ((avg2 - Wk_Param) / avg1) * Wk_Score + Wk_Param;
                // 仮評価が現在の平均以上のとき (公式２） y = (100 - n ) 7 ( 100 - h )  ( x - 100) + 100
                }else if(Wk_Score >= avg1){
                    Wk_Score = ((100 - avg2) / (100 - avg1)) * (Wk_Score - 100) + 100;
                }
            }else if(avg1 >= avg3){     //現在の平均点が60未満のとき
                //仮評価が現在の平均点未満のとき(公式３） y = n 7 ｈ  x
                if(Wk_Score < avg1){
                    Wk_Score = (avg2 / avg1) * Wk_Score;
                // 仮評価が現在の平均以上のとき '(公式４）(公式２と同じ） y = (100 - n ) 7 ( 100 - h )  ( x - 100) + 100
                }else if(Wk_Score >= avg1){
                    Wk_Score = ((100 - avg2) / (100 - avg1)) * (Wk_Score - 100) + 100;
                }
            }
            obj[i].value = Math.round(Wk_Score);
            sum += Wk_Score;
        }
    }
    if (obj.length > 0){
        sum = Math.round((sum/obj.length)*10)/10.0;
        outputLAYER('AVG_VALUATION' , sum);
    }
}
