function btn_submit(cmd) {

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }

    if (cmd == 'subform1_clear'){
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    if ((cmd == 'subform1_delete') && !confirm('{rval MSG103}')){
        return true;
    } else if (cmd == 'subform1_delete'){
        for (var i=0; i < document.forms[0].elements.length; i++)
        {
            if (document.forms[0].elements[i].name == "CHECKED[]" && document.forms[0].elements[i].checked){
                break;
            }
        }
        if (i == document.forms[0].elements.length){
            alert("チェックボックスを選択してください");
            return true;
        }
    }

    if ((cmd == 'subform1_update') && (document.forms[0].SEQ.value == "")){
            alert('{rval MSG308}');
            return true;
    }

    if ((cmd == 'subform1_insert') || (cmd == 'subform1_update')){
        if ((document.forms[0].DISEASE.value == "") && 
            (document.forms[0].S_YEAR.value == "")  && 
            (document.forms[0].S_MONTH.value == "") && 
            (document.forms[0].E_YEAR.value == "")  && 
            (document.forms[0].E_MONTH.value == "") && 
            (document.forms[0].SITUATION.value == "")) {
            alert("データを入力してください");
            return true;
        }

        //期間チェック
        if ((document.forms[0].S_YEAR.value) && 
            (document.forms[0].S_MONTH.value) && 
            (document.forms[0].E_YEAR.value)  && 
            (document.forms[0].E_MONTH.value)){

            if(String(eval(document.forms[0].S_MONTH.value)).length > 1){
                var s_month = eval(document.forms[0].S_MONTH.value);
            } else {
                var s_month = '0' + eval(document.forms[0].S_MONTH.value);
            }
            if(String(eval(document.forms[0].E_MONTH.value)).length > 1){
                var e_month = eval(document.forms[0].E_MONTH.value) ;
            } else {
                var e_month = '0' + eval(document.forms[0].E_MONTH.value);
            }

            var s_date = document.forms[0].S_YEAR.value + '/' + s_month + '/1' ; //期間開始日付
            var e_date = document.forms[0].E_YEAR.value + '/' + e_month + '/1' ; //期間終了日付

            if(s_date > e_date) {
                alert('{rval MSG916}' + '（期間）');
                return true;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//全チェック操作
function check_all(obj){

    for (var i=0; i < document.forms[0].elements.length; i++)
    {
        if (document.forms[0].elements[i].name == "CHECKED[]"){
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

//disabled
function OptionUse(obj)
{
    check_flg = false;

    for (var i=0; i < document.forms[0].elements.length; i++)
    {
        if (document.forms[0].elements[i].checked == true){
            check_flg = true;
        }
    }

	if(check_flg == true)
	{
		document.forms[0].btn_del.disabled = false;
	} else {
		document.forms[0].btn_del.disabled = true;
	}
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
