function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function is_opener() {

    var ua = navigator.userAgent
    if(!!window.opener)
        if( ua.indexOf('MSIE 4')!=-1 && ua.indexOf('Win')!=-1)
             return !window.opener.closed
        else return typeof window.opener.document  == 'object'
    else return false

}
function observeDisp(){
  if (!is_opener()){
      top.window.close();
  }
}

function search_submit(){

    var blank_flg = 0;
    for (var i=0;i<document.forms[0].elements.length;i++)
    {
        var e = document.forms[0].elements[i];
        if (e.type != 'text' && e.type != 'select-one')
            continue;

        if (e.value == '') blank_flg++;
    }
    
    if (blank_flg == 10) {
        alert('{rval MSG300}' + '最低一項目を指定してください。');
        return;
    }

    top.opener.document.forms[0].HR_CLASS.value         = document.forms[0].HR_CLASS.value;
    top.opener.document.forms[0].SRCH_SCHREGNO.value    = document.forms[0].SRCH_SCHREGNO.value;
    top.opener.document.forms[0].NAME.value             = document.forms[0].NAME.value;
    top.opener.document.forms[0].NAME_SHOW.value        = document.forms[0].NAME_SHOW.value;
    top.opener.document.forms[0].NAME_KANA.value        = document.forms[0].NAME_KANA.value;
    top.opener.document.forms[0].NAME_ENG.value         = document.forms[0].NAME_ENG.value;
    top.opener.document.forms[0].BANKCD.value           = document.forms[0].BANKCD.value;
    top.opener.document.forms[0].BRANCHCD.value         = document.forms[0].BRANCHCD.value;
    top.opener.document.forms[0].DEPOSIT_ITEM.value     = document.forms[0].DEPOSIT_ITEM.value;
    top.opener.document.forms[0].ACCOUNTNO.value        = document.forms[0].ACCOUNTNO.value;

    top.opener.document.forms[0].cmd.value = 'search';
    top.opener.document.forms[0].submit();
    top.window.close();
    return false;
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
    console.log(i);
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
