function btn_submit(cmd) 
{
    if (cmd == 'delete') {
        if (document.forms[0].APPLICANTDIV.value == '') {
            alert('{rval MSG301}\n( 入試制度 )');
            return true;
        }
        if (document.forms[0].TESTDIV.value == '') {
            alert('{rval MSG301}\n( 入試区分 )');
            return true;
        }
        if (document.forms[0].BEFORE_PAGE.value == '' || document.forms[0].BEFORE_SEQ.value == '') {
            alert('{rval MSG301}\n( 事前番号 )');
            return true;
        }
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return true;
    }
    if (cmd == 'reference') {
        if (document.forms[0].BEFORE_PAGE.value == '' || document.forms[0].BEFORE_SEQ.value == '') {
            alert('{rval MSG301}\n( 志願番号 )');
            return true;
        }
    }

    if (cmd == 'update') {
        if (document.forms[0].APPLICANTDIV.value == '') {
            alert('{rval MSG301}\n( 入試制度 )');
            return true;
        }
        if (document.forms[0].TESTDIV.value == '') {
            alert('{rval MSG301}\n( 入試区分 )');
            return true;
        }
        if (document.forms[0].BEFORE_PAGE.value == '' || document.forms[0].BEFORE_SEQ.value == '') {
            alert('{rval MSG301}\n( 事前番号 )');
            return true;
        }
        if (document.forms[0].NAME.value == '') {
            alert('{rval MSG301}\n( 氏名 )');
            return true;
        }
        if (document.forms[0].NAME_KANA.value == '') {
            alert('{rval MSG301}\n( 氏名カナ )');
            return true;
        }
        if (document.forms[0].SEX.value == '') {
            alert('{rval MSG301}\n( 性別 )');
            return true;
        }
        if (document.forms[0].FINSCHOOLCD.value == '') {
            alert('{rval MSG301}\n( 出身学校 )');
            return true;
        }
   }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function btn_disabled() {
    document.forms[0].btn_udpate.disabled = true;
    document.forms[0].btn_del.disabled = true;
}

//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}
