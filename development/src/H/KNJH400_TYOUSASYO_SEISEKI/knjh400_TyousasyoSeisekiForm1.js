function btn_submit(cmd) {
    if ((cmd == "delete" || cmd == "delete2") && !confirm("{rval MSG103}")) {
        return true;
    } else if (cmd == "delete" || cmd == "delete2") {
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name == "CHECKED[]" && document.forms[0].elements[i].checked) {
                break;
            }
        }
        if (i == document.forms[0].elements.length) {
            alert("チェックボックスを選択してください");
            return true;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
var selectedRow = 0;
var selectedColor = "";
function selectRow() {
    if (event.srcElement.parentElement.rowIndex == null) {
        return;
    }
    if (selectedColor == "") {
        selectedColor = list.rows[selectedRow].style.backgroundColor;
    }
    list.rows[selectedRow].style.backgroundColor = selectedColor;
    selectedRow = event.srcElement.parentElement.rowIndex;
    selectedColor = list.rows[selectedRow].style.backgroundColor;
    list.rows[selectedRow].style.backgroundColor = "#ccffcc";
    var chk = document.forms[0]["CHECKED[]"];
    if (chk.length) {
        parent.bottom_frame.location.href = "knje063index.php?cmd=edit&CHECKED=" + chk[selectedRow].value;
    } else if (chk) {
        parent.bottom_frame.location.href = "knje063index.php?cmd=edit&CHECKED=" + chk.value;
    }
}
function check_all(obj) {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]" && !document.forms[0].elements[i].disabled) {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}
