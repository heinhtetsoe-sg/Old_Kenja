function btn_submit(cmd) {
    if (cmd == "reference") {
        if (document.forms[0].EXAMNO.value == "") {
            alert("受験番号を入力してください。");
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}

function toTelNo(checkString) {
    var newString = "";
    var count = 0;
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i + 1);
        if ((ch >= "0" && ch <= "9") || ch == "-") {
            newString += ch;
        }
    }

    if (checkString != newString) {
        alert(
            "入力された値は不正な文字列です。\n電話(FAX)番号を入力してください。\n入力された文字列は削除されます。"
        );
        // 文字列を返す
        return newString;
    }
    return checkString;
}

//フォーカスが離れた時にコピー
function toCopytxt(index, txtvalue) {
    switch (index) {
        case 0:
            if (document.forms[0].GZIPCD.value == "") {
                document.forms[0].GZIPCD.value = txtvalue;
                return false;
            }
            break;
    }
}
