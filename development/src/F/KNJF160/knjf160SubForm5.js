function btn_submit(cmd, electdiv) {

    //学籍番号チェック
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }

    if (cmd == 'subform5_clear'){
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//年範囲チェック
function YearCheck(year)
{
    if(year.value && ((year.value < 1900) || (year.value > 2100))){
        alert("{rval MSG913}");
        year.focus();
        return false;
    }
}

//月範囲チェック
function MonthCheck(month)
{
    if(month.value && ((month.value < 1) || (month.value > 12))){
        alert("{rval MSG913}\n　　　　　（ 1月 ～ 12月 ）");
        month.focus();
        return false;
    }
}
