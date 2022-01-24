function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function newwin(SERVLET_URL){

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
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
