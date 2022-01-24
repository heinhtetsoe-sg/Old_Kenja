var openMenu = true;
window.onload = function(){
    if(document.forms[0].cmd.value == 'change'){
        window.open('index.php?cmd=retree','left_frame');
    }
}
function updNotDisp(obj, cmd, menuId, dataDivNo) {
    insertFlg = '1';
    if (!obj.checked) {
        insertFlg = '0';
    }
    window.open('index.php?cmd=' + cmd + '&DATA_DIV_NO=' + dataDivNo + '&insertFlg=' + insertFlg + '&MENUID=' + menuId, 'right_frame');
}
function logout2(){
    
    logoff = true;
    top.location.href='index.php?logout=true';
    var URL = '../../common/logoffmess.php';
    newWin = window.open(URL,"logoff2","left=600,top=200,width=320,height=150");
    newWin.focus();
    
}

function logout3(){
    
    logoff = true;
    top.location.href='index.php?cmd=gakki';
    
}

