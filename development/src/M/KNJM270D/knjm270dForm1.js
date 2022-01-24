function btn_submit(cmd) {
    if (cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//エンターキーをTabに変換
function changeEnterToTab(obj) {
    // Ent13
    var e = window.event;
    if (e.keyCode != 13) {
        return;
    }

    for (var f = 0; f < document.forms[0].length; f++) {
        if (document.forms[0][f].name == obj.name) {
            var targetObject = document.forms[0][(f + 1)];

            if (targetObject.type != 'text') {//テキスト以外はフォーカスしない
                return;
            }
            targetObject.focus();
            return;
        }
    }
    return;
}
