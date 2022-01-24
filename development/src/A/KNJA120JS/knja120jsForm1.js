function btn_submit(cmd){

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'form2'){
        loadwindow('knja120jsindex.php?cmd=form2',0,0,600,500);
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
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
function recvValue(signature, result) {
    document.forms[0].SIGNATURE.value=signature;
    document.forms[0].GOSIGN.value=result;
    document.forms[0].cmd.value = 'sslExe';
    document.forms[0].submit();
}
