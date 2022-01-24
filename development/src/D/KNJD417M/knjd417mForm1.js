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
        // if (document.forms[0].KIND_NAME.value.length > 15) {
        //     alert('{rval MSG901}' + "構成項目名の入力文字数は15文字までです。");
        //     return false;
        // }
        //updateするなら、文字色をクリアする。
        document.forms[0].KIND_NAME_FLG.value = "0";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//項目数チェック
function level(cnt) {
    var level;
    level = document.forms[0].KIND_CNT.value;
    if (level > 0) {
    } else {
        alert('{rval MSG913}'+'\n　　　　　　　　　( 段階数：1 ～ ' + document.forms[0].MAX_KIND_NO.value + ' )');
        document.forms[0].KIND_CNT.focus();
        return false;
    }

    if (level == cnt) {
        return false;
    }
    if (Number(level) > Number(document.forms[0].MAX_KIND_NO.value)) {
        alert('{rval MSG913}'+'\n項目数は' + document.forms[0].MAX_KIND_NO.value + 'を超えてはいけません。');
        document.forms[0].KIND_CNT.focus();
        return false;
    }
    document.forms[0].HID_KIND_CNT.value = document.forms[0].KIND_CNT.value;
    document.forms[0].cmd.value = 'level';
    document.forms[0].submit();
    return false;
}

function resetcolor(obj) {
    obj.style.color = "black";
    document.forms[0].KIND_NAME_FLG.value = "0";
}
