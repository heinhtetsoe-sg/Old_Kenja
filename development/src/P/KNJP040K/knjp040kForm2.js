function btn_submit(cmd) {
   
    if (cmd == "clear") {
        if (!confirm('{rval MSG106}'))
            return false;
    }        
        
    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            alert('{rval MSG203}');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset() {
   result = confirm('{rval MSG106}');
   if (result == false) {
       return false;
   }
}

/* XMLHttpRequest���� */
function createXmlHttp(){
    if( document.all ){
        return new ActiveXObject("Microsoft.XMLHTTP");
    }
    else if( document.implementation ){
        return new XMLHttpRequest();
    }
    else{
        return null;
    }
}
var xmlhttp = null;
function SetBranch(obj){
    /* ���X�|���X�f�[�^���� */
    var handleHttpEvent = function (){
            debug(xmlhttp.readyState);
        if( xmlhttp.readyState == 4 ){
            var data = [];
            if( xmlhttp.status == 200 ){
                var resdata = xmlhttp.responseText;
                debug(resdata);
                if (resdata.length != ""){
                  //�f�R�[�h��eval����JavaScript��
                  eval('data='+ decodeURIComponent(resdata));
                }
                setList(document.forms[0].BRANCHCD, data);
            }
            /* �ʐM�G���[�\��
            else{
                window.alert("�ʐM�G���[���������܂����B");
            }
            */
        }
    }
    /* XMLHttpRequest�I�u�W�F�N�g�쐬 */
    if( xmlhttp == null ){
        xmlhttp = createXmlHttp();
    }
    else{
        /* ���ɍ쐬����Ă���ꍇ�A�ʐM���L�����Z�� */
        xmlhttp.abort();
    }
    /* ���̓t�H�[���f�[�^�̏��� */
    var postdata = new String();
    postdata = "cmd=send";
    postdata += "&BANKCD="+obj.value;
    debug(postdata);
    /* ���X�|���X�f�[�^�������@�̐ݒ� */
    xmlhttp.onreadystatechange = handleHttpEvent;
    /* HTTP���N�G�X�g���s */
    xmlhttp.open("POST", "knjp040kindex.php" , true);
    xmlhttp.setRequestHeader("Content-Type" , "application/x-www-form-urlencoded");
    xmlhttp.send(postdata);

}
function setList(opt, data)
{
  opt.options.length = 0;
  //generating new options
  var j = 0;
  for (var i in data){
    opt.options[j] = new Option(data[i],i);
    j++;
  }
}
function debug(str){
//  document.getElementById("debug").innerHTML = str;
}
