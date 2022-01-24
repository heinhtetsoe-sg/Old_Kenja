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
    //何年用のフォームを使うのか決める
    if (document.forms[0].FORM6.checked) {
        document.forms[0].NENYOFORM.value = document.forms[0].NENYOFORM_CHECK.value
    } else {
        document.forms[0].NENYOFORM.value = document.forms[0].NENYOFORM_SYOKITI.value
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJE";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

