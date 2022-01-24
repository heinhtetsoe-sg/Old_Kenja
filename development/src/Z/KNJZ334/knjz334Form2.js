window.onload = function(){
    if(document.forms[0].cmd.value == 'comp'){
        window.open('knjz334index.php?cmd=list','left_frame');
        window.open('knjz334index.php?cmd=edit','right_frame');
    }
    
}

function btn_submit(cmd) {
    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}')){
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    
    if(cmd == 'add' || cmd == 'update' || cmd == 'indadd' || cmd == 'indupdate'){
        if(document.forms[0].NAIYO.value == ''){
            alert('内容を入力してください');
            document.forms[0].NAIYO.focus();
            return false;
        }
        if(document.forms[0].FROM.value == '' || document.forms[0].TO.value == ''){
            alert('表示期間を入力してください');
            return false;
        }
        if(document.forms[0].FROM.value > document.forms[0].TO.value){
            alert('日付が前後しています');
            return false;
        }
    }

    //サブミット時、一旦、左リストをクリア
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    attribute4 = document.forms[0].selectdataLabel;
    attribute4.value = "";
    //右クラス変更と更新時、左リストを保持
    if (cmd == 'change_grp' || cmd == 'indupdate' || cmd == 'indadd') {
        
        sep = "";
        for (var i = 0; i < document.forms[0].LEFT_PART.length; i++) {
            attribute3.value = attribute3.value + sep + document.forms[0].LEFT_PART.options[i].value;
            attribute4.value = attribute4.value + sep + document.forms[0].LEFT_PART.options[i].text;
            sep = ",";
        }

    }

    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function btn_executeDel(cmd, fileName) {
    document.forms[0].fileName.value = fileName;
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//生徒移動
function moveStudent(side, sort) {
    move(side,'LEFT_PART','RIGHT_PART',sort);

}

function btn_ctrls(e) {
    document.forms[0].btn_add.disabled    = false;
    document.forms[0].btn_update.disabled = false;
    document.forms[0].btn_reset.disabled  = false;
}

function Page_jumper(jump,no)
{
    var cd;
    cd = '?NO=';

        parent.location.replace(jump + cd + no);

}

function Btn_reset(cmd) {
    result = confirm('{rval MSG106}');
    if (result == false) {
        return false;
    }
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
