function getGtreData(gtredata){
   document.forms[0].GTREDATA.value = gtredata;
   document.forms[0].cmd.value = 'main';
   document.forms[0].submit();
   return false;
}
function btn_submit(cmd) {
    if (document.forms[0].OPERATION_DATE.value == "") {
        alert('{rval MSG301}'+'(実施日)');
        return true;
    }
    if (cmd == 'delete_check' && !confirm('{rval MSG103}')){
        return true;
    }
    
    if (document.forms[0].chairSel.value=="") {
        alert('{rval MSG304}'+'(講座名称)');
        return true;    
    }
    if (cmd == 'add_check' || cmd == 'update_check') {
        if(document.forms[0].PERFECT.value=="" ||
           document.forms[0].RATE.value=="" ||
           document.forms[0].TESTCD.value=="" ||
           document.forms[0].OPERATION_DATE.value=="") {
           alert('{rval MSG301}');
           return true;
        } 
    }
    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset(cmd) {
    result = confirm('{rval MSG107}');
    if (result == false) {
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ConfirmOnTermError(cmd) {

    if (confirm('指定した実施日が実施期間外になっています。\nこのまま処理を実行しますか？')) {
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
    }
}

function ConfirmOnDeleteError() { 

    if (confirm('指定したデータに得点データが存在します。\nこのまま処理を実行しますか？')) {
        document.forms[0].cmd.value = 'delete';
        document.forms[0].submit();
        return false;
    }
}