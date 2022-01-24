function btn_submit(cmd) {
    if (cmd == "delete") {
        if (!confirm("{rval MSG103}")) return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
var nowValue;
function setSelected(obj) {
    nowValue = obj.value;
}
function submitCheck(obj) {
    if (nowValue == "") {
        btn_submit("number2");
        return;
    }
    if (confirm("校種または入試区分を変更した場合受験番号データはすべて削除されます。\nよろしいでしょうか？")) {
        btn_submit("number2");
    } else {
        obj.value = nowValue;
        return false;
    }
}
function Page_jumper(link) {
    if (document.forms[0].EXAMNO.value == "") {
        alert("{rval MSG304}");
        return;
    }
    if (!confirm("{rval MSG108}")) {
        return;
    }
    parent.location.href = link;
}
function current_cursor_focus() {}
function current_cursor_list() {}
