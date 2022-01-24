function btn_submit(cmd) {
    if (document.forms[0].GRADE_HR_CLASS.value == '' && 
        document.forms[0].NAME.value == '' && 
        document.forms[0].NAME_KANA.value == ''
        ) {
        alert('この処理は許可されていません。' + '最低一項目を指定してください。');
        return;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//詳細画面へ
function GoWin(URL, hr_name, schregno){
    wopen(URL+'?hr_name='+hr_name+'&SCHREGNO='+schregno, 'SUBWIN2', 0, 0, screen.availWidth, screen.availHeight);
}
