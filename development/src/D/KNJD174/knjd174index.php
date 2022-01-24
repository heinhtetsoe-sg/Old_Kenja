<?php

require_once('for_php7.php');

require_once('knjd174Model.inc');
require_once('knjd174Query.inc');

class knjd174Controller extends Controller {
    var $ModelClassName = "knjd174Model";
    var $ProgramID      = "KNJD174";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd174":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd174Model();       //コントロールマスタの呼び出し
                    $this->callView("knjd174Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd174Ctl = new knjd174Controller;
//var_dump($_REQUEST);
?>
