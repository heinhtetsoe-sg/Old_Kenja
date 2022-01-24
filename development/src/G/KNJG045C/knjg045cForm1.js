function btn_submit(cmd)
{
    if (cmd == 'update') {
        //入力チェック
        if (document.forms[0].DIARY_DATE.value == "") {
            alert("日付が未入力です。");
            document.forms[0].DIARY_DATE.focus();
            return false;
        }
        if(parseInt(document.forms[0].SEQ002_REMARK2.value)<0 || parseInt(document.forms[0].SEQ002_REMARK2.value)>23 ){
            alert('日直の開始時間が不正です。');
            document.forms[0].SEQ002_REMARK2.focus();
            return false;
        }
        if(parseInt(document.forms[0].SEQ002_REMARK3.value)<0 || parseInt(document.forms[0].SEQ002_REMARK3.value)>59 ){
            alert('日直の開始時間が不正です。');
            document.forms[0].SEQ002_REMARK3.focus();
            return false;
        }
        if(parseInt(document.forms[0].SEQ002_REMARK5.value)<0 || parseInt(document.forms[0].SEQ002_REMARK5.value)>23 ){
            alert('日直の終了時間が不正です。');
            document.forms[0].SEQ002_REMARK5.focus();
            return false;
        }
        if(parseInt(document.forms[0].SEQ002_REMARK6.value)<0 || parseInt(document.forms[0].SEQ002_REMARK6.value)>59 ){
            alert('日直の終了時間が不正です。');
            document.forms[0].SEQ002_REMARK6.focus();
            return false;
        }
    } else if ((cmd == 'delete') && !confirm('{rval MSG103}')){
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}