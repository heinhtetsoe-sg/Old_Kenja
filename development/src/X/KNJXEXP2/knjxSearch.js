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

function search_submit(mode){

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
    parent.left_frame.search(document.forms[0],mode);
    return false;
}
function btn_back(){
//    parent.right_frame.location.href = parent.left_frame.document.forms[0].path.value + '&init=1&cmd=serch';
    parent.right_frame.location.href = parent.left_frame.document.forms[0].path.value + '&init=3';
}
