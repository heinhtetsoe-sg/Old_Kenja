function btn_submit(cmd){
    if (cmd == 'shomei' || cmd == 'sslApplet') {
        var flag;
        flag = "";
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            var e = document.forms[0].elements[i];
            if (e.type == "checkbox" && e.checked && e.name == "CHK_DATA[]") {
                flag = "on";
            }
        }
        if (flag == ''){
            alert("チェックボックスが選択されておりません。");
            return;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function chkAll(obj) {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        if (e.type == "checkbox" && !e.disabled && e.name == "CHK_DATA[]") {
            e.checked = obj.checked;
        }
    }
}

function notStampClose() {
    alert('印影登録がありません。');
    deleteCookie();
    closeWin();
}

// クッキーの削除
function deleteCookie() {
    cName = "I9e58bUn";     // 削除するクッキー名(証明書)
    cPass = "hoj3RG8t";   // 削除するクッキー名(パスワード)
    dTime = new Date();
    dTime.setYear(dTime.getYear() - 1);
    document.cookie = cName + "=;expires=" + dTime.toGMTString() + ";path=/";
    document.cookie = cPass + "=;expires=" + dTime.toGMTString() + ";path=/";
}
//Appletから呼出されて、署名を格納。
function recvValue(signature, result, schregno) {
    document.forms[0].SIGNATURE.value=signature;
    document.forms[0].GOSIGN.value=result;
    document.forms[0].SCHREGNO.value=schregno;
    document.forms[0].cmd.value = 'sslExe';
    document.forms[0].submit();
}
