function btn_submit(cmd) {
    if (cmd == "csv") {
        if ((document.forms[0].seito.checked == false) && (document.forms[0].katsudo.checked == false) && (document.forms[0].gakushu.checked == false) && (document.forms[0].tani.checked == false) ) {
            alert('出力する帳票を選択してください。');
            return;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function checkRisyu() {
    if (document.forms[0].RISYUTOUROKU[0].checked && document.forms[0].MIRISYU[1].checked) {
        alert('履修登録のみ科目が出力される状態になっています。');
        document.forms[0].MIRISYU[0].checked = true;
        document.forms[0].MIRISYU[1].checked = false;
        document.forms[0].RISYU[0].checked = true;
        document.forms[0].RISYU[1].checked = false;
    }
    if (document.forms[0].MIRISYU[0].checked && document.forms[0].RISYU[1].checked) {
        alert('未履修科目が出力される状態になっています。');
        document.forms[0].RISYU[0].checked = true;
        document.forms[0].RISYU[1].checked = false;
    }
}
function newwin(SERVLET_URL){
    if ((document.forms[0].seito.checked == false) && (document.forms[0].katsudo.checked == false) && (document.forms[0].gakushu.checked == false) && (document.forms[0].tani.checked == false) ) {
        alert('出力する帳票を選択してください。');
        return;
    }

    var schregno;
    var g_year;
    var g_semester;
    var g_grade;
    var sep;

    schregno = g_year = g_semester = g_grade = sep = "";
    for (var i=0;i<window.parent.left_frame.document.forms[0].elements.length;i++)
    {
        var e = window.parent.left_frame.document.forms[0].elements[i];
        if (e.type=='checkbox' && e.checked && e.name!='chk_all'){
            var val = e.value;
            if (val != ''){
                var tmp = val.split(',');

                schregno += sep+tmp[0];
                g_year += sep+tmp[1];
                g_semester += sep+tmp[2];
                g_grade += sep+tmp[3];
                sep = ",";
            }
        }
    }
    if (schregno != '' && g_year != '' && g_semester != '' && g_grade != ''){
        document.forms[0].SCHREGNO.value = schregno;
        document.forms[0].G_YEAR.value = g_year;
        document.forms[0].G_SEMESTER.value = g_semester;
        document.forms[0].G_GRADE.value = g_grade;
    }else{
        alert("左よりチェックボックスが選択されておりません。");
        return false;
    }

/*****NO001
    var obj2 = document.forms[0].DATE;
    if (obj2.value == '')
    {
        alert("日付が不正です。");
        obj2.focus();
        return false;
    }
*****/

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJI";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
function kubun() 
{
    var kubun1 = document.forms[0].seito;
    var kubun2 = document.forms[0].simei;
    var kubun4 = document.forms[0].katsudo;
    var kubun5 = document.forms[0].gakushu;
    var kubun6 = document.forms[0].tani;

    if ((kubun1.checked == false) && (kubun4.checked == false) && (kubun5.checked == false) && (kubun6.checked == false)) {
        flag3 = true;
    } else {
        flag3 = false;
    }
    document.forms[0].btn_print.disabled = flag3;
    
    if (kubun1.checked == true) {
        flag1 = false;
    } else {
        flag1 = true;
    }
//    document.forms[0].koseki.disabled = flag1;
    document.forms[0].simei.disabled = flag1;
    document.forms[0].schzip.disabled = flag1;
//    document.forms[0].addr2.disabled = flag1;
    document.forms[0].schoolzip.disabled = flag1;
}
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
