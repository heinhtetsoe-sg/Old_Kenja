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
function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function search_submit(){

    var elementCnt = 0;
    for (var i=0;i<document.forms[0].elements.length;i++) {
        var e = document.forms[0].elements[i];
        if (e.type != 'text' && e.type != 'select-one') {
            continue;
        }
        if (e.value != '') {
            elementCnt++;
        }
    }
    if (elementCnt == 0) {
        alert('この処理は許可されていません。' + '最低一項目を指定してください。');
        return;
    }
    top.opener.document.forms[0].SCHREGNO.value = document.forms[0].SCHREGNO.value;
    top.opener.document.forms[0].GRADE_HR_CLASS.value = document.forms[0].GRADE_HR_CLASS.value;
    top.opener.document.forms[0].ATTENDNO.value = document.forms[0].ATTENDNO.value;
    top.opener.document.forms[0].COURSEMAJOR.value = document.forms[0].COURSEMAJOR.value;
    top.opener.document.forms[0].COURSECODE.value = document.forms[0].COURSECODE.value;
    top.opener.document.forms[0].NAME.value = document.forms[0].NAME.value;
    top.opener.document.forms[0].NAMESHOW.value = document.forms[0].NAMESHOW.value;
    top.opener.document.forms[0].KANA.value = document.forms[0].KANA.value;
    top.opener.document.forms[0].cmd.value = 'search';
    top.opener.document.forms[0].submit();
    top.window.close();
    return false;
}

