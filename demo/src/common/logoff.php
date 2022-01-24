<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=<?php

require_once('for_php7.php');
 echo CHARSET ?>">
<link rel="stylesheet" href="gk.css">
<script langage="JavaScript" src="./common.js"></script>
<script langage="JavaScript">
function onload_func(){
    if (chkCookie("Gk_Session")){
        deleteCookie("Gk_Session");
    }
    setInterval('window.close()',2000);
}
window.onload = onload_func;
</script>
</head>
<body bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" text="#000000" link="#006633" vlink="#006633" alink="#006633" background="">
<table width="100%" height="100" border="0" cellspacing="15" cellpadding="0">
<tr>
    <td valign="middle" nowrap>
        <img src="<?php echo REQUESTROOT ."/image/system/log_off.gif" ?>" align="absmiddle">
    </td>
</tr>
</table>
</body>
</html>
<?php 
class LogoffController extends Controller {
    var $ModelClassName = "knjxlogoffModel";
    var $ProgramID      = "KNJXLOGOFF";     //�v���O����ID

    function main(){
        global $auth, $sess;

       //�A�N�Z�X���O�o�^
        common::access_log("logout", 0);
        $auth->logout();
        $sess->delete();
    }
}
$Ctl = new LogoffController;
?>