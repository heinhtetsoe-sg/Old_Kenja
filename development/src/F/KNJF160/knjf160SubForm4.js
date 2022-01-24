function btn_submit(cmd) {

    //学籍番号チェック
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }

    if (cmd == 'subform4_clear'){
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

//disabled
function OptionUse(obj)
{
    //麻疹予防接種
    if(obj = document.forms[0].MEASLES){
        if(document.forms[0].MEASLES.value == "1"){
            document.forms[0].MEASLES_TIMES.disabled = false;
            document.forms[0].MEASLES_YEAR1.disabled = false;
            document.forms[0].MEASLES_MONTH1.disabled = false;
            document.forms[0].MEASLES_YEAR2.disabled = false;
            document.forms[0].MEASLES_MONTH2.disabled = false;
            document.forms[0].MEASLES_YEAR3.disabled = false;
            document.forms[0].MEASLES_MONTH3.disabled = false;
            document.forms[0].VACCINE.disabled = false;
            document.forms[0].LOT_NO.disabled = false;
            document.forms[0].CONFIRMATION.disabled = false;
        } else if(document.forms[0].MEASLES.value == "2"){
            document.forms[0].MEASLES_TIMES.disabled = true;
            document.forms[0].MEASLES_YEAR1.disabled = true;
            document.forms[0].MEASLES_MONTH1.disabled = true;
            document.forms[0].MEASLES_YEAR2.disabled = true;
            document.forms[0].MEASLES_MONTH2.disabled = true;
            document.forms[0].MEASLES_YEAR3.disabled = true;
            document.forms[0].MEASLES_MONTH3.disabled = true;
            document.forms[0].VACCINE.disabled = true;
            document.forms[0].LOT_NO.disabled = true;
            document.forms[0].CONFIRMATION.disabled = false;
        } else {
            document.forms[0].MEASLES_TIMES.disabled = true;
            document.forms[0].MEASLES_YEAR1.disabled = true;
            document.forms[0].MEASLES_MONTH1.disabled = true;
            document.forms[0].MEASLES_YEAR2.disabled = true;
            document.forms[0].MEASLES_MONTH2.disabled = true;
            document.forms[0].MEASLES_YEAR3.disabled = true;
            document.forms[0].MEASLES_MONTH3.disabled = true;
            document.forms[0].VACCINE.disabled = true;
            document.forms[0].LOT_NO.disabled = true;
            document.forms[0].CONFIRMATION.disabled = true;
        }
    }
    //麻疹（罹患歴）
    if(obj = document.forms[0].A_MEASLES){
        if(document.forms[0].A_MEASLES.value == "1"){
            document.forms[0].A_MEASLES_AGE.disabled = false;
            document.forms[0].A_CONFIRMATION.disabled = false;
        } else if(document.forms[0].A_MEASLES.value == "2"){
            document.forms[0].A_MEASLES_AGE.disabled = true;
            document.forms[0].A_CONFIRMATION.disabled = false;
        } else {
            document.forms[0].A_MEASLES_AGE.disabled = true;
            document.forms[0].A_CONFIRMATION.disabled = true;
        }
    }
    //抗体検査履歴
    if(obj = document.forms[0].ANTIBODY){
        if(document.forms[0].ANTIBODY.value == "1"){
            document.forms[0].ANTIBODY_YEAR.disabled = false;
            document.forms[0].ANTIBODY_MONTH.disabled = false;
            document.forms[0].ANTIBODY_POSITIVE.disabled = false;
        } else {
            document.forms[0].ANTIBODY_YEAR.disabled = true;
            document.forms[0].ANTIBODY_MONTH.disabled = true;
            document.forms[0].ANTIBODY_POSITIVE.disabled = true;
        }
    }
}
