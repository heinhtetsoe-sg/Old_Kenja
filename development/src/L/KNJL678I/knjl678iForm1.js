function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    action = document.forms[0].action;
    target = document.forms[0].target;

    //url = location.hostname;
    //document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + '/KNJL';
    document.forms[0].target = '_blank';
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//学校検索画面のプログラムで呼び出す関数を空定義
function current_cursor_focus() {}
function current_cursor_list() {}