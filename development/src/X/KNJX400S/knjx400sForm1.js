function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function notPemClose() {
    alert('証明書ファイルがありません。');
    closeWin();
}

//2011.03.07
//Appletから呼出されて、署名を格納。
function recvValue(signature, seqno, result) {
    document.forms[0].SIGNATURE.value=signature;
    document.forms[0].SEQNO.value=seqno;
    document.forms[0].GOSIGN.value=result;
    document.forms[0].cmd.value = 'sslExe';
    document.forms[0].submit();
}
//Appletから呼出されて、署名を格納。
function recvSeqno(seqno, result) {
    document.forms[0].SEQNO.value=seqno;
    document.forms[0].GOSIGN.value=result;
    document.forms[0].cmd.value = 'seqExe';
    document.forms[0].submit();
}
//Appletから呼出されて、指定INPUT値を戻す
function getvalue(name) {
     return document.getElementsByName(name)[0].value;
}
