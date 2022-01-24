function btn_submit(cmd)
{
    var str = new Object();
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function setName(obj, rowid, flg)
{
    Setflg(obj);    //バックカラーを黄色に変更
    var idx = obj.value;
    if (obj.value == '') {
        if (flg == '0') {
            outputLAYER('STATE_NAME' + rowid, '');
        } else {
            outputLAYER('PERIOD_NAME' + rowid, '');
        }
        return;
    }

    if (flg == '0') {
        if (state_name[idx] != null) {
            outputLAYER('STATE_NAME' + rowid, state_name[idx]);
            if (obj.value == 0){
                document.forms[0].elements[obj.id*2+1].value = '*';
                document.forms[0].elements[obj.id*2+1].readOnly = true ;
                document.forms[0].elements[obj.id*2+2].focus();
            }else {
                if (document.forms[0].elements[obj.id*2+1].value == '*') document.forms[0].elements[obj.id*2+1].value = '';
                document.forms[0].elements[obj.id*2+1].focus();
                document.forms[0].elements[obj.id*2+1].readOnly = false ;
            }
        } else {
            alert('状態区分を指定して下さい。');
            outputLAYER('STATE_NAME' + rowid, '');
            obj.value = '';
            if (obj.id == 0) {
                document.forms[0].elements[obj.id+1].focus();   //OnChangeでは、フォーカスの移動が直接できないので
            } else {
                document.forms[0].elements[obj.id-1].focus();   //OnChangeでは、フォーカスの移動が直接できないので
            }
            obj.focus();                                        //一旦他のテキストへ移動後、再度フォーカスを設定する。
        }
    } else {
        if (pcnt_name[idx] != null) {
            outputLAYER('PERIOD_NAME' + rowid, pcnt_name[idx]);
        } else {
            //document.forms[0].elements[obj.id*2].value：現在行の状態コード
            if(document.forms[0].elements[obj.id*2].value == 0 && obj.value == '*'){
                outputLAYER('PERIOD_NAME' + rowid, '');
            }else if(document.forms[0].elements[obj.id*2].value == 0 && obj.value != '*'){
                alert('校時ＣＤが不正です。\n\n状態【0】は、【*】を指定して下さい。');
                outputLAYER('PERIOD_NAME' + rowid, '');
                obj.value = '';
                if (obj.id == 0) {
                    document.forms[0].elements[0].focus();          //OnChangeでは、フォーカスの移動が直接できないので
                } else {
                    document.forms[0].elements[obj.id-1].focus();   //OnChangeでは、フォーカスの移動が直接できないので
                }
                obj.focus();                                        //一旦他のテキストへ移動後、再度フォーカスを設定する。
            }else {
                alert('校時ＣＤが不正です。');
                outputLAYER('PERIOD_NAME' + rowid, '');
                obj.value = '';
                if (obj.id == 0) {
                    document.forms[0].elements[0].focus();          //OnChangeでは、フォーカスの移動が直接できないので
                } else {
                    document.forms[0].elements[obj.id-1].focus();   //OnChangeでは、フォーカスの移動が直接できないので
                }
                obj.focus();                                        //一旦他のテキストへ移動後、再度フォーカスを設定する。
            }
        }
    }
    return;
}

function Setflg(obj)
{
    change_flg = true;
    if (obj.id){
        obj.style.background="yellow";
        document.getElementById('ROWID' + obj.id).style.background="yellow";
    }
}

function check(obj){

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
