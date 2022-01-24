function btn_submit(cmd) {
    //削除
    if (cmd == "delete") {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }

    //取消
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //職業分類確定ボタン
    if (cmd == 'apply_jobtype') {
        if (document.forms[0].JOBTYPE_SCD.value == '') {
            alert('分類コードを入力してください');
            return false;
        }
    }

    //CSV処理
    if (cmd == 'csv') {
        if (document.forms[0].OUTPUT[1].checked && document.forms[0].FILE.value == '') {
            alert('ファイルを指定してください');
            return false;
        }

        if (document.forms[0].OUTPUT[0].checked) {
            cmd = 'downloadHead';
        } else if (document.forms[0].OUTPUT[1].checked) {
            cmd = 'uploadCsv';
        } else if (document.forms[0].OUTPUT[2].checked) {
            cmd = 'downloadCsv';
        } else if (document.forms[0].OUTPUT[3].checked) {
            cmd = 'downloadError';
        } else {
            alert('ラジオボタンを選択してください。');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

function RetJobtype() {
    alert('該当する職業分類コードはありません');
    document.forms[0].JOBTYPE_SCD.value = "";
    return false;
}
function syugyouCopy(obj) {
    if (obj.checked == true) {
        document.forms[0].SHUSHOKU_ZIPCD.value  = document.forms[0].COMPANY_ZIPCD.value;
        document.forms[0].SHUSHOKU_ADDR1.value  = document.forms[0].COMPANY_ADDR1.value;
        document.forms[0].SHUSHOKU_ADDR2.value  = document.forms[0].COMPANY_ADDR2.value;
        document.forms[0].SHUSHOKU_TELNO1.value = document.forms[0].COMPANY_TELNO1.value;
    } else {
        document.forms[0].SHUSHOKU_ZIPCD.value  = "";
        document.forms[0].SHUSHOKU_ADDR1.value  = "";
        document.forms[0].SHUSHOKU_ADDR2.value  = "";
        document.forms[0].SHUSHOKU_TELNO1.value = "";
    }
    return false;
}