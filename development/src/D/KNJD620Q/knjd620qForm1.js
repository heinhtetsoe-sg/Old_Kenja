function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL, cmd){
    document.forms[0].cmd.value = cmd;
    if (document.forms[0].SDATE.value == '' || document.forms[0].DATE.value == '') {
        alert("出欠集計日付が未入力です");
        return;
    }
    if (document.forms[0].SDATE.value < document.forms[0].YEAR_SDATE.value) {
        alert("出欠集計開始日が年度開始日以前です");
        return;
    }
    if (document.forms[0].SDATE.value > document.forms[0].DATE.value) {
        alert("出欠集計範囲が不正です");
        return;
    }

    //日付範囲チェック
    var day   = document.forms[0].DATE.value.split('/');        //出欠集計日付
    var sdate = document.forms[0].SEME_SDATE.value.split('/');  //学期開始日付
    var edate = document.forms[0].SEME_EDATE.value.split('/');  //学期終了日付

    if((new Date(eval(sdate[0]),eval(sdate[1])-1,eval(sdate[2])) > new Date(eval(day[0]),eval(day[1])-1,eval(day[2])))
       || ((new Date(eval(day[0]),eval(day[1])-1,eval(day[2])) > new Date(eval(edate[0]),eval(edate[1])-1,eval(edate[2])))))
    {
        alert("出欠集計終了日付が学期の範囲外です");
        return;
    }

    if (document.forms[0].TESTKINDCD.value == '') {
        alert("試験が選択されていません。");
        return;
    }
    var ketten = document.forms[0].KETTEN;
    if (ketten) {
        if (ketten.value == '') {
            alert("欠点が不正です。");
            obj1.focus();
            return false;
        }
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function calc(obj) {
    //数字チェック
    if (isNaN(obj.value)){
        alert('{rval MSG907}');
        obj.value = obj.defaultValue;
        return;
    }
}

