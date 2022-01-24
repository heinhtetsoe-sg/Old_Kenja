<?php

require_once('for_php7.php');

function remotelogin()
{
    $user_id = $_REQUEST['autouser'];
    $passwrd_ = $_REQUEST['autopass'];
    $pass2 = $_REQUEST['autopass2'];
    $passwrd = common::passwdDecode($passwrd_, $pass2, true);
//Syslog(LOG_INFO, "呼出し変換{" .$user_id."}{".$pass2."：".$passwrd_."=>".$passwrd."}");
    if ((strlen($user_id)>0)&&(strlen($passwrd)>0)) {
        $autotoken = md5(microtime());
        $expire = time() + 5 * 60;    // 5分有効
        setcookie('autotoken', $autotoken, $expire,'/');
        $passwrd = md5("$user_id:$passwrd:$autotoken");
        header("Location: http://".$_SERVER['SERVER_NAME'].REQUESTROOT."/X/KNJXMENU/index.php?autoid0=".$user_id."&autoid1=".$passwrd);
    }
    else {
        header("Location: http://".$_SERVER['SERVER_NAME'].REQUESTROOT."/index.php");
    }
}
remotelogin();
?>
