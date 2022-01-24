function search_submit(cmd){

    var flg = false;
    for (var i=0;i<document.forms[0].elements.length;i++){
        var e = document.forms[0].elements[i];
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
var opt = {};
function init(){
    for (var i = 0; i < document.forms[0].BRANCHCD.options.length; i++){
        var val = document.forms[0].BRANCHCD.options[i].value;
        var txt = document.forms[0].BRANCHCD.options[i].text;
        opt[val] = txt;
    }
    document.forms[0].BRANCHCD.options.length = 0;
}
function chgBankcd(obj){
    var j = 0;
    document.forms[0].BRANCHCD.options.length = 0;
    document.forms[0].BRANCHCD.options[j] = new Option();
    document.forms[0].BRANCHCD.options[j].text = '　　　　';
    document.forms[0].BRANCHCD.options[j].value = '';
    j++;
    for (var i in opt){
        var a = i.split("-");
        if (a[0] == obj.value){
            document.forms[0].BRANCHCD.options[j] = new Option();
            document.forms[0].BRANCHCD.options[j].text = opt[i];
            document.forms[0].BRANCHCD.options[j].value = i;
            j++;
        }
    }
}
window.onload = init;