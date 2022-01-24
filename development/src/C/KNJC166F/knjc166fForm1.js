function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function isHankakuNum(vl) {
    return !vl.match(/[^0-9+$]/);
}

function newwin(SERVLET_URL, flg){

    //日付チェック
    if (document.forms[0].DATE.value == "") {
        alert('日付を指定してください。');
        return false;
    }
    if (document.forms[0].DATE.value < document.forms[0].SDATE.value ||
        document.forms[0].DATE.value > document.forms[0].EDATE.value) {
        alert("日付が学期範囲外です。");
        return;
    }
    
    if (document.forms[0].DISP_CONDITION.value == "1") {
        if (document.forms[0].knjc166fDispType.value == "1") {
            if (document.forms[0].KEKKA_KESSEKI_CNT.value == '') { 
                alert('{rval MSG301}' + "回数を指定してください");
                return;
            }
            if (!isHankakuNum(document.forms[0].KEKKA_KESSEKI_CNT.value)) {
                alert('{rval MSG907}' + "：回数");
                return;
            }
        } else {
            if (document.forms[0].BUNKYO_KANSAN_KESSEKI.value == '') { 
                alert("回数を指定してください");
                return;
            }
            if (document.forms[0].BUNKYO_KANSAN_KESSEKI_NOZOKU
                && document.forms[0].BUNKYO_KANSAN_KESSEKI_NOZOKU.value != ''
                && parseInt(document.forms[0].BUNKYO_KANSAN_KESSEKI.value) <= parseInt(document.forms[0].BUNKYO_KANSAN_KESSEKI_NOZOKU.value)) { 
                alert("回数の指定が不正です");
                return;
            }
        }
    }

    document.forms[0].OUTPUTCSV.value = flg;
    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJC";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

