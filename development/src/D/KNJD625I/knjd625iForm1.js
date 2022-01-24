function btn_submit(cmd) {
    if (cmd == "copy") {
        if (document.forms[0].PRE_YEAR_CNT.value <= 0) {
            alert('前年度の対象データが存在しません。');
            return false;
        }
        if (document.forms[0].THIS_YEAR_CNT.value > 0) {
            if (!confirm('今年度のデータを更新します。コピーしてもよろしいですか？')) {
                return false;
            }
        } else {
            if (!confirm('{rval MSG101}')) {
                return false;
            }
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
    var chk = document.forms[0]["DATACD"];
    parent.right_frame.location.href ='knjd625iindex.php?cmd=edit&DATACD='+chk[selectedRow].value;
}