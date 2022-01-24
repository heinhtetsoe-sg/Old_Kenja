function btn_submit(cmd) {
    //削除
    if(cmd == 'delete' && !confirm('{rval MSG103}')) { 
        return true;
    }
    //取消
    if(cmd == 'reset' && !confirm('{rval MSG106}')) { 
        return false;
    }
    //追加
    if(cmd == 'add') {
        //日付チェック（学期範囲）
        var day   = document.forms[0].DIARY_DATE.value.split('/');  //日付
        var sdate = document.forms[0].SEME_SDATE.value.split('-');  //学期開始日付
        var edate = document.forms[0].SEME_EDATE.value.split('-');  //学期終了日付
        if ((new Date(eval(sdate[0]),eval(sdate[1])-1,eval(sdate[2])) > new Date(eval(day[0]),eval(day[1])-1,eval(day[2])))
           || ((new Date(eval(day[0]),eval(day[1])-1,eval(day[2])) > new Date(eval(edate[0]),eval(edate[1])-1,eval(edate[2])))))
        {
            alert("日付が年度の範囲外です");
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function selcheck(that) {

    //全角から半角
    that.value = toHankakuNum(that.value);
    //数値型へ変換
    that.value = toInteger(that.value);
    //セルが空の時０
    if(that.value == '' ){
        that.value = 0;
        return;
    }
}

//区分別テキストボックス内禁止処理
function check(that) {

    checktest(that.value);

}

function checktest(num) {

    switch (num) {
        case "1":
            document.forms[0].TRANSFER_EDATE.disabled = false;
            document.forms[0].TRANSFERPLACE.disabled = false;
            document.forms[0].TRANSFERADDR.disabled = false;
            document.forms[0].ABROAD_CLASSDAYS.disabled = false;
            document.forms[0].ABROAD_CREDITS.disabled = false;
            document.all('TRANSFER_EDATE').style.backgroundColor = "white";
            document.all('TRANSFERPLACE').style.backgroundColor = "white";
            document.all('TRANSFERADDR').style.backgroundColor = "white";
            document.all('ABROAD_CLASSDAYS').style.backgroundColor = "white";
            document.all('ABROAD_CREDITS').style.backgroundColor = "white";
            break;
        case "2":
        case "3":
            document.forms[0].TRANSFER_EDATE.disabled = false;
            document.forms[0].TRANSFERPLACE.disabled = true;
            document.forms[0].TRANSFERADDR.disabled = true;
            document.forms[0].ABROAD_CLASSDAYS.disabled = true;
            document.forms[0].ABROAD_CREDITS.disabled = true;
            document.all('TRANSFER_EDATE').style.backgroundColor = "white";
            document.all('TRANSFERPLACE').style.backgroundColor = "darkgray";
            document.all('TRANSFERADDR').style.backgroundColor = "darkgray";
            document.all('ABROAD_CLASSDAYS').style.backgroundColor = "darkgray";
            document.all('ABROAD_CREDITS').style.backgroundColor = "darkgray";
            break;
        case "4":
            document.forms[0].TRANSFER_EDATE.disabled = true;
            document.forms[0].TRANSFERPLACE.disabled = true;
            document.forms[0].TRANSFERADDR.disabled = true;
            document.forms[0].ABROAD_CLASSDAYS.disabled = true;
            document.forms[0].ABROAD_CREDITS.disabled = true;
            document.all('TRANSFER_EDATE').style.backgroundColor = "darkgray";
            document.all('TRANSFERPLACE').style.backgroundColor = "darkgray";
            document.all('TRANSFERADDR').style.backgroundColor = "darkgray";
            document.all('ABROAD_CLASSDAYS').style.backgroundColor = "darkgray";
            document.all('ABROAD_CREDITS').style.backgroundColor = "darkgray";
            break;
    }
}
