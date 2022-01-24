function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function f_dragstart(event) {
    event.dataTransfer.setData("text", event.target.id);
}
function f_dragover(event) {
    event.preventDefault();
}

document.onkeydown = function (event) {
    if (event.keyCode == 8) {
        if (typeof event.target.tagName !== "undefined" && event.target.tagName !== false) {
            tagName = event.target.tagName.toLowerCase();
        }
        if (tagName !== "input") {
            return false;
        }
    }
};
