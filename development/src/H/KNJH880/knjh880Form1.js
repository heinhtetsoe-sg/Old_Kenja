function btn_submit(cmd) {
    
    if(cmd == "hyouzi"){
        if(document.forms[0].CHAIRCD.value == ""){
            alert('表示対象となる生徒の学級・講座を選択してください。');
            return false;
        }
        if(document.forms[0].KYOKA.value == ""){
            alert('教科を選択してください。');
            return false;
        }

        if((document.forms[0].F_HOUR.value != "" && document.forms[0].F_MIN.value == "")
            || (document.forms[0].T_HOUR.value != "" && document.forms[0].T_MIN.value == "")
            || (document.forms[0].F_HOUR.value == "" && document.forms[0].F_MIN.value != "")
            || (document.forms[0].T_HOUR.value == "" && document.forms[0].T_MIN.value != "")){
            alert('時間の指定が正しくありません。');
            return false;
        }
        if(document.forms[0].F_HOUR.value == "" && document.forms[0].T_HOUR.value != ""){
            alert('開始時間を指定してください。');
            return false;
        }
        if(document.forms[0].F_HOUR.value != "" && document.forms[0].DATE.value == ""){
            alert('日付を選択してください。');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function check_all(obj) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "DELCHK[]" && !document.forms[0].elements[i].disabled) {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

function orderBy(order)
{
    document.forms[0].ORDER.value = order;
    
    btn_submit('order');
}
