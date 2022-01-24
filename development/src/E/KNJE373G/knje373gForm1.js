//サブミット
function btn_submit(cmd) {
    if (cmd == "csv"){
        if (document.forms[0].YEAR.value == ""){
            alert('基準年度を入力して下さい。');
            return false;
        }

        if (document.forms[0].YEARS.value == ""){
            alert('指定年間を入力して下さい。');
            return false;
        }
    }
    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
