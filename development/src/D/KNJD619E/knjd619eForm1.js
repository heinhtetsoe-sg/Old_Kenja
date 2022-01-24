function btn_submit(cmd) {

    if (cmd == "csv"){
        //選択チェック（テスト項目）
        if (document.forms[0].TESTKINDCD.value == '') {
            alert('{rval MSG304}\n　　（テスト項目）');
            return;
        }
        //選択学年名・選択学期名・選択考査種別名をhiddenで保持
        attribute1 = document.forms[0].GRADE;
        attribute2 = document.forms[0].SEMESTER;
        attribute3 = document.forms[0].TESTKINDCD;
        document.forms[0].selectGradeName.value = attribute1.options[attribute1.selectedIndex].text;
        document.forms[0].selectSemeName.value = attribute2.options[attribute2.selectedIndex].text;
        document.forms[0].selectTestName.value = attribute3.options[attribute3.selectedIndex].text;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
