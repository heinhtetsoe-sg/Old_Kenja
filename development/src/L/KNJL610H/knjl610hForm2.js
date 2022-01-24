function btn_submit(cmd) {
    //削除
    if (cmd == "delete") {
        if (!confirm("{rval MSG103}")) {
            return false;
        }
    }

    //取消
    if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }

    //CSV処理
    if (cmd == "csv") {
        if (
            document.forms[0].OUTPUT[1].checked &&
            document.forms[0].FILE.value == ""
        ) {
            alert("ファイルを指定してください");
            return false;
        }

        if (document.forms[0].OUTPUT[0].checked) {
            cmd = "headerCsv";
        } else if (document.forms[0].OUTPUT[1].checked) {
            cmd = "uploadCsv";
        } else if (document.forms[0].OUTPUT[2].checked) {
            cmd = "downloadCsv";
        } else {
            alert("ラジオボタンを選択してください。");
            return false;
        }
    }
    document.forms[0].REMARK8.value = document.forms[0].REMARK8.value.replace(/\r\n/g, '').replace(/\r|\n/g, '');
    document.forms[0].REMARK9.value = document.forms[0].REMARK9.value.replace(/\r\n/g, '').replace(/\r|\n/g, '');
    document.forms[0].REMARK10.value = document.forms[0].REMARK10.value.replace(/\r\n/g, '').replace(/\r|\n/g, '');
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}
