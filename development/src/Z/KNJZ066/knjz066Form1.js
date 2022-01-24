function btn_submit(cmd) {
    //取消
    if (cmd == "clear") {
        if (!confirm('{rval MSG106}')) {
            return false;
        } 
    }

    //削除
    if (cmd == "delete") {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }

    //更新
    if (cmd == "update") {
        //確定された段階数で処理
        if (document.forms[0].LEVEL.value != document.forms[0].LEVELCNT.value) {
            document.forms[0].LEVELCNT.value = document.forms[0].LEVEL.value;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//段階数チェック
function level(cnt) {
    var level;
    level = document.forms[0].LEVELCNT.value;

    if (level > 0) {
    } else {
        alert('{rval MSG913}'+'\n　　　　　　　　　( 段階数：1～100 )');
        document.forms[0].LEVELCNT.focus();
        return false;
    }

    if (level == cnt) {
        return false;
    }

    if (level > 100) {
        alert('{rval MSG913}'+'\n段階数は100を超えてはいけません。');
        document.forms[0].LEVELCNT.focus();
        return false;
    }

    document.forms[0].cmd.value = 'level';
    document.forms[0].submit();
    return false;
}
