function btn_submit(cmd) {
    if(cmd == 'exec'){
        if(document.forms[0].CHOICE.value == "2"){
            //試験会場指定時
            if(document.forms[0].PLACE_COMB.value == ""){
                alert('試験会場を選択してください。');
                return false;
            }
        }else if(document.forms[0].CHOICE.value == "3"){
            if(document.forms[0].EXAM_FROM.value == ""){
                alert('受験番号を指定してください。');
                document.forms[0].EXAM_FROM.focus();
                return false;
            }else if(document.forms[0].EXAM_TO.value != "" && document.forms[0].EXAM_FROM.value > document.forms[0].EXAM_TO.value){
                alert('指定した受験番号が前後しています。');
                return false;
            }else{
                //HIDDENに入れていた値
                var from = document.forms[0].AREA_FROM.value;
                var to = document.forms[0].AREA_TO.value;
                
                if(document.forms[0].STUDENT.value == "1"){
                    //駿中生指定時
                    if(document.forms[0].EXAM_FROM.value < from 
                       || (document.forms[0].EXAM_TO.value != "" && to < document.forms[0].EXAM_TO.value) 
                       || to < document.forms[0].EXAM_FROM.value){
                        alert('指定された受験番号が駿中生の範囲外です。');
                        return false;
                    }
                }else{
                    //その他指定時
                    if((from <= document.forms[0].EXAM_FROM.value && document.forms[0].EXAM_FROM.value <= to)
                       || (document.forms[0].EXAM_TO.value != "" && from <= document.forms[0].EXAM_TO.value && document.forms[0].EXAM_TO.value <= to)){
                        alert('指定された受験番号はその他の生徒の範囲外です。');
                        return false;
                    }
                }
            }
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
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
	if(document.forms[0].CHOICE.value == "2"){
		//試験会場指定時
		if(document.forms[0].PLACE_COMB.value == ""){
			alert('試験会場を選択してください。');
			return false;
		}
	}else if(document.forms[0].CHOICE.value == "3"){
		if(document.forms[0].EXAM_FROM.value == ""){
			alert('受験番号を指定してください。');
			document.forms[0].EXAM_FROM.focus();
			return false;
		}else if(document.forms[0].EXAM_TO.value != "" && document.forms[0].EXAM_FROM.value > document.forms[0].EXAM_TO.value){
			alert('指定した受験番号が前後しています。');
			return false;
		}else{
			//HIDDENに入れていた値
			var from = document.forms[0].AREA_FROM.value;
			var to = document.forms[0].AREA_TO.value;
			
			if(document.forms[0].STUDENT.value == "1"){
				//駿中生指定時
				if(document.forms[0].EXAM_FROM.value < from 
				   || (document.forms[0].EXAM_TO.value != "" && to < document.forms[0].EXAM_TO.value) 
				   || to < document.forms[0].EXAM_FROM.value){
					alert('指定された受験番号が駿中生の範囲外です。');
					return false;
				}
			}else{
				//その他指定時
				if((from <= document.forms[0].EXAM_FROM.value && document.forms[0].EXAM_FROM.value <= to)
				   || (document.forms[0].EXAM_TO.value != "" && from <= document.forms[0].EXAM_TO.value && document.forms[0].EXAM_TO.value <= to)){
					alert('指定された受験番号はその他の生徒の範囲外です。');
					return false;
				}
			}
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

