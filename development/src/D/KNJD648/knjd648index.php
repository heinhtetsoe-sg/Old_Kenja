<?php

require_once('for_php7.php');

require_once('knjd648Model.inc');
require_once('knjd648Query.inc');

class knjd648Controller extends Controller {
    var $ModelClassName = "knjd648Model";
    var $ProgramID      = "KNJD648";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd648":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd648Model();       //コントロールマスタの呼び出し
                    $this->callView("knjd648Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd648Ctl = new knjd648Controller;
//var_dump($_REQUEST);
?>
