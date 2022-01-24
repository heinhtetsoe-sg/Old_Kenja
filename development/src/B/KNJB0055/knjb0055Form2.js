function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

//アプレットを開く
function Page_jumper(URL,year,semester,subclasscd,prgid,staffcd,cntSubclassStdDat) {
    if (cntSubclassStdDat < 1) {
        alert('この科目は誰も登録していません。');
        return false;
    }
    if (subclasscd == '') {
        alert('{rval MSG304}');
        return false;
    }
    wopen(URL+'?year='+year+'&semester='+semester+'&subclasscd='+subclasscd+'&staffcd='+staffcd,'name',0,0,screen.availWidth,screen.availHeight);
}
