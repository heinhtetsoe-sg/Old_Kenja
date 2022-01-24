function btn_submit(cmd) {

    //チェック
    if ((cmd == 'update') || (cmd == 'insert')){
        if (document.forms[0].SCHREGNO.value == ""){
            alert('{rval MSG304}');
            return true;
        } else if ((document.forms[0].VISIT_DATE.value == "") || (document.forms[0].VISIT_HOUR.value == "") || (document.forms[0].VISIT_MINUTE.value == "")){
            alert('来室日時が入力されていません。\n　　　　（必須入力）');
            return true;
        } else if (document.forms[0].VISIT_REASON1.value == "") {
            alert('来室理由１が入力されていません。\n　　　　（必須入力）');
            return true;
        } else if (document.forms[0].TREATMENT1.value == "") {
            alert('処置１が入力されていません。\n　　　　（必須入力）');
            return true;
        }
    } else if (cmd == 'subform1_clear'){
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function checkDecimal(obj) {
    var decimalValue = obj.value
    var check_result = false;

    if (decimalValue != '') {
        //空じゃなければチェック
        if (decimalValue.match(/^[0-9]+(\.[0-9]+)?$/)) {
            check_result = true;
        }
    } else {
        check_result = true;
    }

    if (!check_result) {
        alert('数字を入力して下さい。');
        obj.value = '';
    }

    //正しい値ならtrueを返す
    return check_result;
}

//disabled
function OptionUse(obj)
{
    if (document.forms[0].CONDITION4[0].checked == true) {
        document.forms[0].MEAL.disabled = false;
        document.forms[0].MEAL.style.backgroundColor = "#ffffff";
    } else {
        document.forms[0].MEAL.disabled = true;
        document.forms[0].MEAL.style.backgroundColor = "#D3D3D3";
    }
}

//disabled
function OptionUse2(obj, checkText) {
    var array = document.forms[0][checkText].value.split(',');

    flg = true;
    for (var i = 0; i < array.length; i++) {
        if (array[i] == obj.value) {
            flg = false;
        }
    }

    document.forms[0][obj.name+'_TEXT'].disabled = flg;
}
