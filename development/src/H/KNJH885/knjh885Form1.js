function btn_submit(cmd) {
    
    if(cmd == 'csv'){
        if(document.forms[0].KUBUN.value == ''){
            alert('対象を選択してください。');
            return false;
        }else if(document.forms[0].KUBUN.value == '1'){
            //生徒
            if(document.forms[0].G_HR.value == '' || document.forms[0].GHR_CHOICE.value == ''){
                alert('対象生徒を選択してください。');
                return false;
            }
        }
    }else if(cmd == 'import'){
        if(document.forms[0].FILE.value == ''){
            alert('取込ファイルを指定してください。');
            return false;
        }
        if(document.forms[0].KUBUN.value == ''){
            alert('取込対象を選択してください。');
            return false;
        }
    }

    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function check_all(obj) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECK[]" && !document.forms[0].elements[i].disabled) {
            document.forms[0].elements[i].checked = obj.checked;
        }else if(document.forms[0].elements[i].name == "CHECKALL"){
            if(document.forms[0].elements[i].value == "1"){
                document.forms[0].elements[i].value = "";
            }else{
                document.forms[0].elements[i].value = "1";
            }
        }
    }
}

