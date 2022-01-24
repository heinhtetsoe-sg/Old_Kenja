function btn_submit(cmd) {

    if (cmd == 'output') {
        var val_year = document.forms[0].YEAR.value;
        var val_scho = document.forms[0].SCHOOLCD.value;
        var val_majo = document.forms[0].MAJORCD.value;
        var val_saik = document.forms[0].SAIKEN.value;
        if (val_year == '' || 
            val_scho == '' || 
            val_majo == '') {
            alert('{rval MSG301}');
            return false;
        }
        if (document.forms[0].RADIO[0].checked && val_saik == '') {
            alert('{rval MSG301}');
            return false;
        }
        if (val_year.length != '4' || 
            val_scho.length != '5' || 
            val_majo.length != '3') {
            alert('{rval MSG901}\n桁数が違います。全桁入力して下さい。');
            return false;
        }
        if (document.forms[0].RADIO[0].checked && val_saik.length != '3') {
            alert('{rval MSG901}\n桁数が違います。全桁入力して下さい。');
            return false;
        }
    }

    if (cmd == 'check') {
        if (document.forms[0].YEAR.value.length != '4') {
            alert(年度の項目を入力して下さい。);
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
