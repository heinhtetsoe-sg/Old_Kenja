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
        //確定した項目数で処理
        if (document.forms[0].COMPVAL.value != document.forms[0].COMPCNT.value) {
            document.forms[0].COMPCNT.value = document.forms[0].COMPVAL.value;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//項目数チェック
function level(cnt) {
    var level;
    level = document.forms[0].COMPCNT.value;
    if (level > 0) {
    } else {
        alert('{rval MSG913}'+'\n　　　　　　　　　( 段階数：1～99 )');
        document.forms[0].COMPCNT.focus();
        return false;
    }

    if (level == cnt) {
        return false;
    }

    document.forms[0].cmd.value = 'level';
    document.forms[0].submit();
    return false;
}

function resetBaseTitleNamecolor(obj,i) {
    obj.style.color = "red";

    var fixedName = "BASETITLENAME_INFLG_";  //変更項目名称
    for (var cnt=0; cnt < document.forms[0].elements.length; cnt++) {
        if (document.forms[0].elements[cnt].name.indexOf(fixedName) >= 0) {
            var idx = document.forms[0].elements[cnt].name.substr(fixedName.length);
            if (idx == i) {
                //変更項目名称で指定インデックスの値を変更する
                document.forms[0].elements[cnt].value = "0";
            }
        }
    }
}

