function btn_submit(cmd) {
    if (document.forms[0].EXAMNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }
    if (cmd == "delete") {
        if (!confirm("{rval MSG103}")) return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
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
