function btn_submit(cmd) {
    if (document.forms[0].SELECT_DATE.value == "") {
        alert('日付を入力して下さい');
        return false;
    }
    
    if(document.forms[0].SELECT_DATE.value<=maxdate){
    	alert(maxdate+'以降を指定してください。');
        return false;
    }
    if(sdate>document.forms[0].SELECT_DATE.value||edate<document.forms[0].SELECT_DATE.value){
    	alert(sdate+'～'+edate+'の範囲で指定してください。');
        return false;
    }

    parent.document.forms[0].SELECT_DATE.value = document.forms[0].SELECT_DATE.value;
    parent.btn_submit(cmd);
    parent.closeit();
}
