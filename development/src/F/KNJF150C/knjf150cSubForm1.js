function btn_submit(cmd) {

    //チェック
    if ((cmd == 'update') || (cmd == 'insert')){
        if (document.forms[0].SCHREGNO.value == ""){
            alert('{rval MSG304}');
            return true;
        } else if ((document.forms[0].SEQ01_REMARK1.value == "") || (document.forms[0].SEQ01_REMARK2.value == "") || (document.forms[0].SEQ01_REMARK3.value == "")){
            alert('来室日時が入力されていません。\n　　　　（必須入力）');
            return true;
        } else if (document.forms[0].SEQ02_REMARK1.value == "") {
            alert('来室理由が入力されていません。\n　　　　（必須入力）');
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

//印刷
function newwin(SERVLET_URL){

    alert('登録された内容が印刷されます。');

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJF";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//disabled
function OptionUse(obj)
{
	if(document.forms[0].CONDITION4[0].checked == true)
	{
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

function setChkBox(obj, targetName) {
    if (obj.checked) {
        var array = new Array();
        for (var i=0; i < document.forms[0].elements.length; i++) {
            //部分一致
            if (document.forms[0].elements[i].name.indexOf(targetName) === 0){
                array.push(document.forms[0].elements[i]);
            }
        }

        for (var i = 0; i < array.length; i++) {
            if(array[i].type == "checkbox"){
                if(array[i].name != obj.name){
                    array[i].checked = false;
                }
            }
        }
    }
}

function setValue(obj) {
    if(obj.value == ''){
        if(obj.name == 'SEQ03_REMARK1'){
            obj.value = '21';
            obj.selectedIndex = 22;
            obj.options[22].selected = true;
        }
        if(obj.name == 'SEQ03_REMARK3'){
            obj.value = '06';
            obj.selectedIndex = 7;
            obj.options[7].selected = true;
        }
    }
}

function setTime(obj, targetName) {
    if(obj.value != ''){
        if(obj.name == 'SEQ01_REMARK2' || obj.name == 'SEQ01_REMARK3'){
            //来室時間 ⇒ 時間(バイタル1)
            if(document.forms[0][targetName].value == ''){
                document.forms[0][targetName].value = obj.value;
            }
        }
        if(obj.name == 'SEQ09_REMARK4' || obj.name == 'SEQ09_REMARK5'){
            if(document.forms[0].SEQ09_REMARK3.checked==true) {
                //早退 ⇒ 退出時間
                if(document.forms[0][targetName].value == ''){
                    document.forms[0][targetName].value = obj.value;
                }
            }
        }
    }
}