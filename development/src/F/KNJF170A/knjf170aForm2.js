function btn_submit(cmd) {
    //日付範囲チェック
    if (cmd == "add" || cmd == "update" || cmd == "delete") {
        if (document.forms[0].CAMPUS_DIV.value == "") {
            alert('{rval MSG310}');
            return false;
        }
        if (document.forms[0].DATE.value == "") {
            alert('{rval MSG301}');
            return false;
        }

        chk_date = document.forms[0].DATE.value.replace("/","-");
        chk_date = chk_date.replace("/","-");
        sdate = document.forms[0].YEAR.value+'-04-01';
        edate = parseInt(document.forms[0].YEAR.value)+1+'-03-31';

        if (chk_date < sdate || edate < chk_date) {

            chk_date = document.forms[0].DATE.value.replace("-","/");
            chk_date = chk_date.replace("-","/");
            sdate = document.forms[0].YEAR.value+'/04/01';
            edate = parseInt(document.forms[0].YEAR.value)+1+'/03/31';

            alert('{rval MSG916}\n'+sdate+'～'+edate);
            return false;
        }
    }

    temp = parseFloat(document.forms[0].TEMPERATURE.value);

    if ((temp < "-50.0") || ("50.0" < temp)) {
            alert('{rval MSG916}\n      (気温)');
            return false;
    }

    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }

    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        } else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError() {
   alert('{rval MSG300}');
   closeWin();
}

//天気コンボ[5]の時だけテキスト
function textDisabled() {
    if (document.forms[0].WEATHER.value != "5") {
        document.forms[0].WEATHER_TEXT.disabled = "true";
    } else {
        document.forms[0].WEATHER_TEXT.disabled = "";
    }
}

//欠席状況の計算
function calc(obj, di_cd) {
    //数値チェック
    obj.value = toInteger(obj.value);

    //加算
    var total_cnt = 0;
    re = new RegExp("^CNT_"+di_cd );
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(re)) {
            var val = parseInt(document.forms[0].elements[i].value);
            if (val > 0) {
                total_cnt += val;
            }
        }
    }

    //加算した値を貼り付け
    id = 'total_'+di_cd;
    var total = document.getElementById(id);
    total.innerHTML = total_cnt;
}
