function btn_submit(cmd) {
    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }
    if (document.forms[0].OUTPUT[1].checked == false) {
        if (cmd != ""){
            cmd = "csv";
        }
    }

    if (cmd == "csv"){
        //選択チェック（考査種別）
        if (document.forms[0].TESTKINDCD.value == '') {
            alert('{rval MSG304}\n　　（考査種別）');
            return;
        }
        //選択学期名・選択考査種別名をhiddenで保持
        attribute1 = document.forms[0].SEMESTER;
        attribute2 = document.forms[0].TESTKINDCD;
        document.forms[0].selectSemeName.value = attribute1.options[attribute1.selectedIndex].text;
        document.forms[0].selectTestName.value = attribute2.options[attribute2.selectedIndex].text;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}