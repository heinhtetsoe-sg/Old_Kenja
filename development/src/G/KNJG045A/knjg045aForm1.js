function btn_submit(cmd)
{
    if (cmd == 'update') {
        //入力チェック
        if (document.forms[0].SCHOOL_KIND.value === "") {
            alert("校種が未入力です。");
            return false;
        }
        //入力チェック
        if (document.forms[0].DIARY_DATE.value == "") {
            alert("日付が未入力です。");
            document.forms[0].DIARY_DATE.focus();
            return false;
        }
    }
    if (cmd == 'lesson') {
        //入力チェック
        //実際は、起こりえないエラーチェック
        if (document.forms[0].KYOUSEI_SEMESTER.value == "") {
            alert("日付が学期範囲外です。");
            document.forms[0].DIARY_DATE.focus();
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function datecheck(dval)
{
    var chflg = 0;
    //Nullチェック
    if (dval == '') {
        return '';
    }
    //日付正規チェック
    if (!isDate2(dval)) {
       return '';
    }
    //日付の一致チェック
    if (dval == document.forms[0].DEFOULTDATE.value) {
        return '';
    }
    if (sem == 0){
        sem = 1;
        document.forms[0].cmd.value = 'dsub';
        document.forms[0].submit();
        return false;
    }
}

function newwin(SERVLET_URL)
{
    //入力チェック
    if (document.forms[0].DIARY_DATE.value == "") {
        alert("日付が未入力です。");
        document.forms[0].DIARY_DATE.focus();
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJG";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
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
