function btn_submit(cmd) {
    if (cmd == 'change_grade') {
        document.forms[0].change_grade.value = 'on';
        cmd = 'main';
    } else {
        document.forms[0].change_grade.value = 'off';
    }

    if (cmd == 'subExecute') {
        if (!confirm('{rval MSG101}')) return false;

        var check_grade = document.forms[0].CHECK_GRADE.value;

        if (document.forms[0].MAX_SEMES_CL.value != '') {
            alert('{rval MSG311}');
            return false;
        }
        if (document.forms[0].NEXT_SEMESTER.value != '') {
            alert('{rval MSG305}' + '\n' + '次年度の学期が設定されていません。')
            return false;
        }
        if (document.forms[0].FRESHMAN_DAT.value == '' && document.forms[0].FRESHMAN_DATE.value != '' &&
            (check_grade == document.forms[0].CHECK_GRADE_P.value ||
                check_grade == document.forms[0].CHECK_GRADE_J.value ||
                check_grade == document.forms[0].CHECK_GRADE_H.value)
        ) {
            var tdate = document.forms[0].syoribi.value.replace(/\//g, '');
            if (sdate > tdate || tdate > edate) {
                alert('{rval MSG901}' + '\n' + '(新入生入学日付)');
                return false;
            }
        }
        if (document.forms[0].REPET_SCHREGNO.value != '' &&
            (check_grade == document.forms[0].CHECK_GRADE_P.value ||
                check_grade == document.forms[0].CHECK_GRADE_J.value ||
                check_grade == document.forms[0].CHECK_GRADE_H.value)
        ) {
            alert('{rval MSG302}' + '\n' + '(新入生学籍番号)');
            return false;
        }
        if (document.forms[0].CLASS_FORMATION.value != '' && check_grade != '99') {
            alert('{rval MSG305}' + '\n' + 'クラス編成データに未設定のデータがあります。' + '\n' + '(組,出席番号,コースのいずれか)');
            return false;
        }
        if (document.forms[0].STILL_GRD_NO.value != '' && check_grade == '99') {
            alert('{rval MSG305}' + '\n' + '卒業生台帳番号未採番者が存在します。');
            return false;
        }
        if (document.forms[0].FRESHMAN_DAT.value != '' &&
            (check_grade == document.forms[0].CHECK_GRADE_P.value ||
                check_grade == document.forms[0].CHECK_GRADE_J.value ||
                check_grade == document.forms[0].CHECK_GRADE_H.value)
        ) {
            if (!confirm('{rval MSG305}' + '\n' + '新入生移行データにデータが存在しません。' + '\n' + '処理を継続しますか?')) {
                return false;
            }
        }
        if (document.forms[0].GRD_STUDENT.value != '' && check_grade == '99') {
            if (!confirm('{rval MSG303}' + '\n' + '卒業生が存在しません。' + '\n' + '処理を継続しますか?')) {
                return false;
            }
        }
    }
    if (cmd == 'update') {
        if (document.forms[0].useKnja050_select_schoolKind.value == '1' &&
            document.forms[0].SCHOOL_KIND.value == ''
        ) {
            alert('校種を選択して下さい。');
            return false;
        }

        if (!confirm('{rval MSG101}')) return false;

        for (var i = 0; i < document.forms[0].elements.length; i++) {
            var div = document.forms[0].elements[i];
            if (div.name == 'WHICH_WAY') {
                if (div.value == '1' && document.forms[0].GRD_NO.value == '') {
                    alert('{rval MSG301}' + '\n' + '開始台帳番号');
                    return false;
                }
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function close_window() {
    alert('{rval MSG300}');
    closeWin();
}
