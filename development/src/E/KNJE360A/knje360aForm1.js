function btn_submit(cmd) {
    //データ指定チェック
    if (cmd == 'update') {
        if (!document.forms[0].SCHOOLCD.value) {
            alert('{rval MSG304}\n学校が選択されていません。');
            return false;
        }
        if (!document.forms[0].SEQ_LIST.value) {
            alert('{rval MSG303}');
            return false;
        }
    }
    //取消確認
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        updateFrameLockNotMessage(this);
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function allCheck(obj) {
    var checkTaisyou = eval("/^TAISYOU-" + "/");
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(checkTaisyou)) {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}
function newwin(SERVLET_URL){
    var i;
    if (!document.forms[0].SEQ_LIST.value) {
        alert('{rval MSG303}');
        return false;
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

