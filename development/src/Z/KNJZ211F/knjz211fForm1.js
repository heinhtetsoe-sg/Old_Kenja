function btn_submit(cmd) {
    //取消
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }
    //確定
    if (cmd == 'kakutei') {
        if (!(document.forms[0].MAX_ASSESSLEVEL.value > 0)) {
            alert('{rval MSG916}\n( 評定段階数に1以上を指定してください。 )');
            return false;
        }
    }
    if (cmd == 'update2') {
        if (confirm('更新しますか？')){
        	if(document.forms[0].CLASSCD_ALL_CHECK.checked){
    			document.forms[0].cmd.value = 'update2_no';
        	} else {
    			document.forms[0].cmd.value = 'update2_yes';
    		}
    	} else {
    		return false;
    	}
    } else {
    	if(cmd == 'delete' || cmd == 'update'){
    		if(document.forms[0].elements['DIV'].value == 1 && 
    		   document.forms[0].IS_HUITTI.value != '' && 
    		   (parseInt(document.forms[0].IS_HUITTI.value) != (parseInt(document.forms[0].IS_HUITTI2.value)+parseInt(document.forms[0].elements['ASSESSLEVEL-0'].value)))){
                if (!confirm("”評価・評定の段階値”は削除されます。\nよろしいですか？")){
                	return false;
    			} else {
    				document.forms[0].IS_HUITTI_FLAG.value=1;
    			}
    		} else if(document.forms[0].elements['DIV'].value == 1 && cmd == 'delete') {
    			if(!confirm("”評価・評定の段階値”は削除されます。\nよろしいですか？")){
    				return false;
    			}
    		} else if(cmd == 'delete') {
    			if(!confirm('{rval MSG103}')){
    				return false;
    			}
    		}
    	}
	    //サブミット
	    document.forms[0].cmd.value = cmd;
    }
	document.forms[0].submit();
    return false;
}

//上限値自動計算
function isNumb(that,level){
    var num;
    var anser;

    that.value = toNumber(that.value);

    if(that.value <= 0){
        return;
    }else{
        anser = (that.value - 1);
        document.getElementById('ASSESSHIGH_ID' + level).innerHTML = anser;
        AssesslowObject  = eval("document.forms[0].Assesshighvalue" + level);
        AssesslowObject.value  = anser;        
    }
    return;
}

//値チェック
    function NumCheck(num) {
    //数値チェック
    num = toInteger(num);

    //範囲チェック
    if (num.length > 0 && num == 0) {
        alert('{rval MSG916}\n( 評定段階数に1以上を指定してください。 )');
        num = '';
    }

    return num;
}
