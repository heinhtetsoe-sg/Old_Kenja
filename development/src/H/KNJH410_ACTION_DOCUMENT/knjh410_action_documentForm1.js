function btn_submit(cmd)
{
	if (cmd != 'subEnd'){
        if ("" == document.forms[0].ACTIONDATE.value || 
            0 == document.forms[0].TITLE.value.replace(/^[ 　]+/,"").length) {
            alert('{rval MSG301}');
            return true;
        }
        if ("" == document.forms[0].ACTIONHOUR.value &&
            "" != document.forms[0].ACTIONMINUTE.value) {
            alert('分のみの入力は、禁止です。\n行動時間を入力してください。');
            return true;
        }
        if ("" != document.forms[0].ACTIONHOUR.value &&
            "" == document.forms[0].ACTIONMINUTE.value) {
            document.forms[0].ACTIONMINUTE.value = '00';
        }
        if ("" != document.forms[0].ACTIONHOUR.value &&
            "" != document.forms[0].ACTIONMINUTE.value) {
            if (document.forms[0].ACTIONHOUR.value < 0 || document.forms[0].ACTIONHOUR.value > 24) {
                alert('時間は、0～24で指定して下さい。');
                return true;
            }
            if (document.forms[0].ACTIONMINUTE.value < 0 || document.forms[0].ACTIONMINUTE.value > 59) {
                alert('分は、0～59で指定して下さい。');
                return true;
            }
        }

	    document.forms[0].cmd.value = cmd;
    	document.forms[0].submit();
	    return false;
	}

	top.main_frame.right_frame.closeit();
    top.main_frame.right_frame.document.forms[0].cmd.value = cmd;
    top.main_frame.right_frame.document.forms[0].submit();
    return false;
}
