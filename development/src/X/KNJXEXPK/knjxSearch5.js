function search_submit(cmd){

    var flg = false;
    for (var i=0;i<document.forms[0].elements.length;i++){
        var e = document.forms[0].elements[i];
        if (e.name == "EXPENSE_L_CD" || e.name == "EXPENSE_M_CD") continue;
        if ((e.type == 'text' || e.type == 'select-one') && e.value != ''){
            flg = true;
            break;
        }
    }
    if (!flg) {
        alert('{rval MSG301}' + '最低一項目を指定してください。');
        return true;
    }
    document.forms[0].target = "left_frame";
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function btn_back(){
    parent.right_frame.location.replace("index.php?cmd=right");
}
function chgCombo(obj){
    var l = document.forms[0]["EXPENSE_L_CD"].value;
    var m = document.forms[0]["EXPENSE_M_CD"].value;
    if (obj.name == "EXPENSE_L_CD"){
        var cmb = "EXPENSE_L_CD";
    }else{
        var cmb = "EXPENSE_M_CD";
    }
    location.replace("index.php?cmd=show&showno=5&EXPENSE_L_CD="+l+"&EXPENSE_M_CD="+m+"&cmb="+cmb);
    return false;
}
