function btn_submit(cmd) {
    //削除確認
    if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return false;
    }
    //取消確認
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return false;
    }

    if (cmd == 'insert' || cmd == 'update' || cmd == 'delete') {
        //必須チェック
        if (document.forms[0].GET_SCHOOL_KIND.value == 'H') {
            if (!document.forms[0].GROUP_CD.value) {
                alert('{rval MSG310}\n( コースグループ )');
                return false;
            }
        } else {
            if (!document.forms[0].COURSE_MAJOR.value) {
                alert('{rval MSG310}\n( コース )');
                return false;
            }
        }
        if (!document.forms[0].SUBCLASS.value) {
            alert('{rval MSG310}\n( 科目 )');
            return false;
        }

        if (cmd == 'insert' || cmd == 'update') {
            //範囲チェック
            if (!(document.forms[0].RATE.value > 0)) {
                alert('{rval MSG901}\n　( 割合：1～100 )');
                return false;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function calc(obj) {
    var val = obj.value;

    //数値チェック
    val = toInteger(val);

    //範囲チェック
    if (val > 100) {
        alert('{rval MSG914}');
        val = "";
    }

    return val;
}
