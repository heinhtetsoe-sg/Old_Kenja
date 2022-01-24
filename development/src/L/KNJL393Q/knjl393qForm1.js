function btn_submit(cmd) {
    if(cmd == 'exec'){
        var i;
        var c_check = 0;
        var cnt = document.forms[0].CHECK_CNT.value;
        for(i=0;i<cnt;i++){
            if(document.getElementById("CHECK"+i).checked){
                c_check = 1;
            }
        }
        if(c_check != 1){
            alert('印刷するリストを選択してください。');
            return false;
        }
        if(document.forms[0].CHOICE.value == '2'){
            if(document.forms[0].EXAM_FROM.value == ''){
                alert('受験番号を指定してください。');
                document.forms[0].EXAM_FROM.focus();
                return false;
            }else if(document.forms[0].EXAM_TO.value != '' && document.forms[0].EXAM_FROM.value > document.forms[0].EXAM_TO.value){
                alert('受験番号の指定範囲が不正です。');
                return false;
            }
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function radio_change()
{
    if(document.getElementById("CHECK5").checked){
        document.getElementById("GROUPNAME1").disabled = false;
        document.getElementById("GROUPNAME2").disabled = false;
    }else{
        document.getElementById("GROUPNAME1").disabled = true;
        document.getElementById("GROUPNAME2").disabled = true;
    }
}

//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

//セキュリティーチェック
function OnSecurityError()
{
    alert('{rval MSG300}' + '\n高セキュリティー設定がされています。');
    closeWin();
}
function newwin(SERVLET_URL){
	var i;
	var c_check = 0;
	var cnt = document.forms[0].CHECK_CNT.value;
	for(i=0;i<cnt;i++){
		if(document.getElementById("CHECK"+i).checked){
			c_check = 1;
		}
	}
	if(c_check != 1){
		alert('印刷するリストを選択してください。');
		return false;
	}
	if(document.forms[0].CHOICE.value == '2'){
		if(document.forms[0].EXAM_FROM.value == ''){
			alert('受験番号を指定してください。');
			document.forms[0].EXAM_FROM.focus();
			return false;
		}else if(document.forms[0].EXAM_TO.value != '' && document.forms[0].EXAM_FROM.value > document.forms[0].EXAM_TO.value){
			alert('受験番号の指定範囲が不正です。');
			return false;
		}
	}

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

