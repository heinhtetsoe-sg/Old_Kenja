function btn_submit(cmd) {
    if (document.forms[0].SCH_CHR_PERIODCD.value == "") {
        alert("{rval MSG304}");
        return false;
    }
    //親画面にパラメタ設定
    var parentForm = parent.document.forms[0];
    if (parentForm.SCH_CHR_EXECUTEDATE) {
        parentForm.SCH_CHR_EXECUTEDATE.value =
            document.forms[0].SCH_CHR_EXECUTEDATE.value;
        parentForm.SCH_CHR_PERIODCD.value =
            document.forms[0].SCH_CHR_PERIODCD.value;
        parentForm.SCH_CHR_CHAIRCD.value =
            document.forms[0].SCH_CHR_CHAIRCD.value;
    }

    document.forms[0].btn_select.disabled = true;
    document.forms[0].btn_back.disabled = true;

    parentForm.cmd.value = cmd;
    parentForm.submit();
    // top.closeit();
    return false;
}

function closeMethod() {
    // parent.document.forms[0].submit();
    parent.closeit();
    return false;
}

function dateChange(object) {
    var dateValue = object;
    if (!dateValue) {
        return false;
    }
    document.forms[0].cmd.value = "schChrList";
    document.forms[0].submit();
}

var selectedRow = 0;
var selectedRowStyle = "";
function selectRow(obj, periodCd, chairCd) {
    document.forms[0].SCH_CHR_PERIODCD.value = periodCd;
    document.forms[0].SCH_CHR_CHAIRCD.value = chairCd;

    if (event.srcElement.parentElement.rowIndex == null) {
        return;
    }
    if (selectedRowStyle == "") {
        selectedRowStyle = schList.rows[0].childNodes[9].getAttribute("style");
    }
    schList.rows[selectedRow].bgColor = "white";
    schList.rows[selectedRow].childNodes[9].setAttribute(
        "style",
        selectedRowStyle
    );
    selectedRow = event.srcElement.parentElement.rowIndex;
    selectedRowStyle = schList.rows[selectedRow].childNodes[9].getAttribute(
        "style"
    );
    schList.rows[selectedRow].bgColor = "#ccffcc";
    schList.rows[selectedRow].childNodes[9].setAttribute(
        "style",
        "background-color:#ccffcc;color:#000000"
    );

    return false;
}

function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}

function selectRowDbl() {
    setTimeout(function () {
        btn_submit("schChrSelect");
    }, 300);
}
