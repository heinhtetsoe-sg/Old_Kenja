function btn_submit() {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    //得点チェック
    var pass_score = document.forms[0].PASS_SCORE.value;
    var kouho_score = document.forms[0].KOUHO_SCORE.value;

    if (pass_score == "") {
        alert("得点が不正です。");
        document.forms[0].SDATE.focus();
        return false;
    }

    if (kouho_score == "") {
        alert("得点が不正です。");
        document.forms[0].EDATE.focus();
        return false;
    }

    if (parseInt(kouho_score) > parseInt(pass_score)) {
        alert("得点の大小が不正です。");
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
