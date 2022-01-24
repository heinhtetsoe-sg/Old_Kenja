function btn_submit(cmd) {
    if (cmd == 'execute') {
        if (document.forms[0].GRADE.options.length == 0 || document.forms[0].EXAM.options.length == 0) {
            return false;
        }

        if (document.forms[0].CHAIRDATE.value == '') {
            alert('日付を入力して下さい。');
            return false;
        }

        var chairdate = document.forms[0].CHAIRDATE.value.split('/');
        var sdate = document.forms[0].SDATE.value.split('/');
        var edate = document.forms[0].EDATE.value.split('/');

        if (new Date(eval(sdate[0]), eval(sdate[1]) - 1, eval(sdate[2])) > new Date(eval(chairdate[0]), eval(chairdate[1]) - 1, eval(chairdate[2])) || new Date(eval(chairdate[0]), eval(chairdate[1]) - 1, eval(chairdate[2])) > new Date(eval(edate[0]), eval(edate[1]) - 1, eval(edate[2]))) {
            alert('日付が学期の範囲外です');
            return false;
        }

        if (confirm('{rval MSG101}')) {
            document.getElementById('marq_msg').style.color = '#FF0000';
        } else {
            return;
        }

        document.forms[0].btn_exec.disabled = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
//選択科目グレーアウト
function disElectdiv() {
    var subcd = document.forms[0].SUBCLASSCD.value;
    if (subcd == '999999' || subcd == '000000') {
        document.forms[0].ELECTDIV.disabled = false;
    } else {
        document.forms[0].ELECTDIV.disabled = true;
    }
    //評定マスタ作成グレーアウト
    if (document.forms[0].knjd210v_useRelativeAssessCheckbox.value == '1') {
        var seme = document.forms[0].SEMESTER.value;
        var test = document.forms[0].EXAM.value;
        if (seme == '9' && test == '99-00-08' && subcd !== '999999') {
            document.forms[0].ASSESS_CHK.disabled = false;
        } else {
            document.forms[0].ASSESS_CHK.disabled = true;
        }
    }
}
