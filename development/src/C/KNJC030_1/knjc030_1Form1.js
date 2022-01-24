function btn_submit(cmd)
{
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function IsUserOK_ToJump(URL, syoribi, period, class_staffcd, ccd, tr_cd1)
{
//    window.open(URL+'?syoribi='+syoribi+'&periodcd='+period+'&STAFFCD='+class_staffcd+'&chaircd='+ccd+'&tr_cd1='+tr_cd1);
    wopen(URL+'?syoribi='+syoribi+'&periodcd='+period+'&STAFFCD='+class_staffcd+'&chaircd='+ccd+'&tr_cd1='+tr_cd1,'name',0,0,screen.availWidth,screen.availHeight);//2006/03/15 alp アルプ
    closeWindow()
}
function closeWindow()
{
    closeWin();
}

