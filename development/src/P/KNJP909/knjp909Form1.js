//サブミット
function btn_submit(cmd) {
    if (cmd == 'update' && !confirm('{rval MSG101}')) {
        return false;
    }

    //読み込み中は、実行ボタンはグレーアウト
    document.forms[0].btn_upd.disabled = true;
    if (cmd == 'update') {
        document.getElementById('marq_msg').style.color = '#FF0000';
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();

    return false;
}
//印刷
function newwin(SERVLET_URL){
    if (document.forms[0].GRADE.value == "") {
        alert("学年を指定してください。");
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJP";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
// 学年進行処理チェック
function closeCheck(){
        alert('{rval MSG300}'+'\n学年進行処理が行われていません。');
        closeWin();
}
