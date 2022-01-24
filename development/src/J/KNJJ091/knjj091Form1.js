function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}


function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

var selectedRow = 1;
function selectRow() {
    var list_table = document.getElementById('list_table');

    if (event.srcElement.parentElement.rowIndex == 0) {
        return false;
    }

    if (event.srcElement.parentElement.rowIndex == null) {
        return;
    }
    list_table.rows[selectedRow].bgColor = "white";
    selectedRow = event.srcElement.parentElement.rowIndex;
    if (list_table.rows[selectedRow].cells[3].firstChild.tagName != 'A') {
        return;
    }

    list_table.rows[selectedRow].bgColor = "#ccffcc";

    parent.edit_frame.location.href = list_table.rows[selectedRow].cells[3].firstChild.href;
}


