function btn_submit(cmd) {
	//群コードを変更した場合、一時的に更新・削除ボタンを押せないようにする（再読込の処理中）
    if (cmd == 'group'){
	    document.forms[0].btn_udpate.disabled = true;
	    document.forms[0].btn_del.disabled = true;
    }
	//削除ボタン押し下げ時
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
	//取消ボタン押し下げ時
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }
	//受講クラス選択ボタン押し下げ時
    if (cmd == 'subform1'){
	    param = document.forms[0].GRADE_CLASS.value;
        loadwindow('knjb0030oindex.php?cmd=subform1&param='+param,0,0,350,400);
        return true;
    }
	//科目担任選択ボタン押し下げ時
    if (cmd == 'subform2'){
        if(document.forms[0].SUBCLASSCD.value == ""){
            alert('科目を選択してください。');
            return false;
        } else {
            param = document.forms[0].STAFFCD.value;
            param2 = document.forms[0].STF_CHARGE.value;
            subclass = document.forms[0].SUBCLASSCD.value;
            loadwindow('knjb0030oindex.php?cmd=subform2&param='+param+'&param2='+param2+'&subclass='+subclass,0,0,350,400);
            return true;
        }
    }
	//使用施設選択ボタン押し下げ時
    if (cmd == 'subform3'){
	    param = document.forms[0].FACCD.value;
        loadwindow('knjb0030oindex.php?cmd=subform3&param='+param,0,0,350,400);
        return true;
    }
	//教科書選択ボタン押し下げ時
    if (cmd == 'subform4'){
	    param = document.forms[0].TEXTBOOKCD.value;
        loadwindow('knjb0030oindex.php?cmd=subform4&param='+param,0,0,350,400);
        return true;
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
//名簿入力画面へ
function Page_jumper(URL,year,semester,chaircd,groupcd,staffcd)
{
    if(chaircd == '' || groupcd == ''){
        alert('{rval MSG304}');
        return false;
    }
//2006.03.15
//    window.open(URL+'?year='+year+'&semester='+semester+'&chaircd='+chaircd+'&groupcd='+groupcd+'&staffcd='+staffcd);
    wopen(URL+'?year='+year+'&semester='+semester+'&chaircd='+chaircd+'&groupcd='+groupcd+'&staffcd='+staffcd,'name',0,0,screen.availWidth,screen.availHeight);
}

