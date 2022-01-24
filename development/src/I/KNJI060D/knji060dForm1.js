function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function checkRisyu() {
    if (document.forms[0].MIRISYU[0].checked && document.forms[0].RISYU[1].checked) {
        alert('未履修科目が出力される状態になっています。');
        document.forms[0].RISYU[0].checked = true;
        document.forms[0].RISYU[1].checked = false;
    }
}
function newwin(SERVLET_URL){

    var schregno;
    var g_year;
    var g_semester;
    var g_grade;
    var sep;
    var i;
    var e;
    var val;
    var tmp;

    //何年用のフォームを使うのか決める
    if (document.forms[0].FORM6 && document.forms[0].FORM6.checked) {
        document.forms[0].NENYOFORM.value = document.forms[0].NENYOFORM_CHECK.value
    } else {
        document.forms[0].NENYOFORM.value = document.forms[0].NENYOFORM_SYOKITI.value
    }
    if (document.forms[0].tyousasyoCheckCertifDate.value == '1' && document.forms[0].DATE.value == '') {
        alert('記載日付を指定してください。');
        return;
    }

    schregno = g_year = g_semester = g_grade = sep = "";
    for (i = 0; i < window.parent.left_frame.document.forms[0].elements.length; i++)
    {
        e = window.parent.left_frame.document.forms[0].elements[i];
        if (e.type=='checkbox' && e.checked && e.name!='chk_all') {
            val = e.value;
            if (val != '') {
                tmp = val.split(',');

                schregno += sep+tmp[0];
                g_year += sep+tmp[1];
                g_semester += sep+tmp[2];
                g_grade += sep+tmp[3];
                sep = ",";
            }
        }
    }
    if (schregno != '' && g_year != '' && g_semester != '' && g_grade != '') {
        document.forms[0].SCHREGNO.value = schregno;
        document.forms[0].G_YEAR.value = g_year;
        document.forms[0].G_SEMESTER.value = g_semester;
        document.forms[0].G_GRADE.value = g_grade;
    } else {
        alert("左よりチェックボックスが選択されておりません。");
        return false;
    }

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
function gvalCalcChecked() {
    var gvalCalcCheck1 = document.getElementById("GVAL_CALC_CHECK1"); // 多重平均
    var ids = ["PRINT_AVG_RANK1", "PRINT_AVG_RANK2"];
    var obj;
    var dis = !(gvalCalcCheck1 && gvalCalcCheck1.checked);
    for (var i in ids) {
        obj = document.getElementById(ids[i]);
        if (obj) {
            obj.disabled = dis;
        }
    }
}
window.addEventListener("load", function(e) {
    gvalCalcChecked();
});

