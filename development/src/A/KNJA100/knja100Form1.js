function btn_submit(cmd) {
    if (cmd == 'execute') {
        var sele_seme = document.forms[0].semester.value;
        var ctrl_seme = document.forms[0].ctrl_semester.value;
        if (sele_seme == ctrl_seme) {
            alert('現在処理学期と生成対象学期が同一の為処理できません');
            return;
        }
        if (confirm('{rval MSG101}')) {
        } else {
        return;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function close_window(cmd) {

   switch (cmd){
    case 1:
        alert('{rval MSG300} \n最終学期では処理できません');
    break;
    case 2:
        alert('{rval MSG300}');
    break;
    case 3:
        alert('{rval MSG305} \nクラス編成データに未設定のデータがあります。')
    break;
        }

        closeWin();

    return true;
}
