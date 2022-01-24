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
function is_opener() {

    var ua = navigator.userAgent
    if(!!window.opener)
        if( ua.indexOf('MSIE 4')!=-1 && ua.indexOf('Win')!=-1)
             return !window.opener.closed
        else return typeof window.opener.document  == 'object'
    else return false

}
function observeDisp(){
  if (!is_opener()){
      top.window.close();
  }
}

function SearchResult(){
    alert('{rval MSG303}');
}
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

function check_all(){
    var flg;
    
    for (var i=0;i<document.forms[0].elements.length;i++)
    {
        var e = document.forms[0].elements[i];
        if (e.name == "chk_all"){
            flg = e.checked;
        }
        if (e.type=='checkbox' && e.name != "chk_all"){
            e.checked = flg;
        }
    }
}

function newwin(SERVLET_URL){
    var schregno;
    var g_year;
    var g_semester;
    var g_grade;
    var sep;

    schregno = g_year = g_semester = g_grade = sep = "";
    for (var i = 0; i < document.forms[0].elements.length;i++) {
        var e = document.forms[0].elements[i];
        if (e.type == 'checkbox' && e.checked && e.name != 'chk_all'){
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
        alert("チェックボックスが選択されておりません。");
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
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
