function btn_submit(cmd) {
    if (cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

// 判定値変更
function CheckJudged(obj) {

    // 判定の入力チェック
    if (obj.value) {
        var isJudge = false;
        for (let i = 0; i < document.forms[0].JUDGEDIVOPT.options.length; i++) {
            const option = document.forms[0].JUDGEDIVOPT.options[i];
            if (obj.value == option.value) {
                // 判定の文字を変更
                document.getElementById('ROWID'+obj.id).cells[7].innerText = option.innerText;
                isJudge = true;
            }
        }
        if (!isJudge) {
            alert('{rval MSG913}');
            return;
        }
    }

    // 合格コースの表示／非表示
    document.getElementsByName('SUC_COURSE-'+obj.id)[0].style.display ="none";
    if (obj.value == '3') {
        document.getElementsByName('SUC_COURSE-'+obj.id)[0].style.display ="block";
    }



}

//欠席チェックON/OFFで背景色を黄色表示
function bgcolorYellow(obj, examno) {
    change_flg = true;

    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].TESTDIV.disabled = true;

    document.getElementById('ROWID' + examno).style.background = obj.checked ? "yellow" : "white";
}

