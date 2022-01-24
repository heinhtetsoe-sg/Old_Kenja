function btn_submit(cmd) {
        if (cmd == 'execute') {
            if (document.forms[0].month.options.length == 0) {
                return false;
            }

            if (confirm("この処理を実行すると『出欠入力制御日付』以前の出欠入力処理は更新権限がなければ処理できません。\nよろしいですか？")) {
                document.all('marq_msg').style.color = '#FF0000';
            } else {
                return;
            }
        }
        if (cmd == 'execute2'){
            cmd = 'execute';
            document.forms[0].cmd2.value = '1';
        }
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

function closing_window(flg){
    if (flg == 1) {
        alert('{rval MSG300}');
    }
    if (flg == 2) {
        alert('{rval MSG305}' + '\n(学期マスタ)');
    }
    closeWin();
    return true;
}


function newwin(SERVLET_URL){
        action = document.forms[0].action;
        target = document.forms[0].target;

//      url = location.hostname;
//      document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
        document.forms[0].action = SERVLET_URL +"/KNJC";
        document.forms[0].target = "_blank";
        document.forms[0].submit();

        document.forms[0].action = action;
        document.forms[0].target = target;
//  }
}
