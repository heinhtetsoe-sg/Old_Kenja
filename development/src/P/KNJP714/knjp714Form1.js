function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function popupGrp(schregNo, slipNo){
    loadwindow('knjp714index.php?cmd=grpform&SEND_SCHREGNO=' + schregNo + '&SEND_SLIP_NO=' + slipNo, 0, 0, 700, 350);
}

//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
//スクロール
function scrollRC(){
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}
