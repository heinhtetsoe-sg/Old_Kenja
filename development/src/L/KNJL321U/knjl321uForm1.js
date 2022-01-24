function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//印刷
function newwin(SERVLET_URL) {
    //必須チェック
    if (document.forms[0].APPLICANTDIV.value == '') {
        alert('{rval MSG310}\n( 入試制度 )');
        return;
    }
    if (document.forms[0].TESTDIV.value == '') {
        alert('{rval MSG310}\n( 入試区分 )');
        return;
    }
    if (document.forms[0].NOTICEDATE.value == '') {
        alert('{rval MSG310}\n( 通知日付 )');
        return;
    }

    var arr = ["A", "B", "C"];
    var i, id1, id2, id3;
    for (i = 1; i <= 3; i++) {
        id1 = document.getElementById("OUTPUT" + i); 
        id2 = document.getElementById("OUTPUT" + arr[i - 1] + "3"); 
        id3 = document.getElementById("EXAMNO" + arr[i - 1]); 
        if (id1 && id1.checked &&
            id2 && id2.checked &&
            id3 && id3.value == "") {
            alert("受験番号を入力して下さい");
            return;
        }
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
