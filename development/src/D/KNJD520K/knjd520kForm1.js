function closing_window(no)
{
    var msg;

    if(no == 'year'){
      msg = '{rval MSG305}';
    }else{
      msg = '{rval MSG300}';
    }

    alert(msg);
    closeWin();
    return true;
}

//--start
function btn_submit(cmd)
{
    if(cmd == 'cancel' && !confirm('{rval MSG106}')) {
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function calc(obj){

    var str = obj.value;
    var nam = obj.name;

    //小文字を大文字に変換
    if (str.toUpperCase() == 'KK' || str.toUpperCase() == 'KS') { 
        obj.value = str.toUpperCase();
        return;
    }

    //数字チェック
    if (isNaN(obj.value)){
        alert('{rval MSG907}');
        obj.value = obj.defaultValue;
        return;
    }

    var score = parseInt(obj.value);
    if(score>100){
    alert('{rval MSG914}'+'0点～100点以内で入力してください。');
        obj.value = obj.defaultValue;
        return;
    }

    var score = parseInt(obj.value);
    if(score<0){
    alert('{rval MSG914}'+'0点～100点以内で入力してください。');
        obj.value = obj.defaultValue;
        return;
    }

}