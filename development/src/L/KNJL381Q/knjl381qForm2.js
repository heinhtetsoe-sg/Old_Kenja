window.onload = function(){

}

function btn_submit(cmd) {   

    if(cmd == 'insert'){
        var min = document.forms[0].MIN0.value;
        var max = document.forms[0].MAX0.value;
        if(min == "" || max == ""){
            alert('点数を入力してください。');
            return false;
        }
        if(Number(min) > Number(max)){
            alert('点数入力が正しくありません。');
            document.forms[0].MIN0.focus();
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function closecheck() {
    parent.window.close();
}

function gamen_reload(kamoku) {

    parent.bottom_frame.location.href='knjl381qindex.php?cmd=edit&KAMOKU='+kamoku;
    
}
