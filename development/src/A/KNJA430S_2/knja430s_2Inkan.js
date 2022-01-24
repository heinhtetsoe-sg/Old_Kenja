function inkan_submit() {
    top.opener.document.forms[0].cmd.value = 'inkan_view';
    top.opener.document.forms[0].submit();
    top.window.close();
    return false;
}
//取込ボタン
function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//2009/10/26
//Appletから呼出されて、Base64変換ビットマップを格納
function recvValue(scan_result,inkan_bmp,inkan_no) {
    document.forms[0].scan_result.value=scan_result;
    document.forms[0].inkan_bmp.value=inkan_bmp;
    //document.forms[0].inkan_no.value=inkan_no;
    if ((scan_result=='non')||(inkan_bmp.length<=0))
        inkan_submit();
    else {
        document.forms[0].cmd.value = 'execute';
        document.forms[0].submit();
    }
}
