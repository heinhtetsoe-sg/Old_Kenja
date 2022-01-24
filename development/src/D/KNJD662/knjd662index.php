<?php

require_once('for_php7.php');

require_once('knjd662Model.inc');
require_once('knjd662Query.inc');

class knjd662Controller extends Controller {
    var $ModelClassName = "knjd662Model";
    var $ProgramID      = "KNJD662";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd662":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd662Model();       //コントロールマスタの呼び出し
                    $this->callView("knjd662Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd662Ctl = new knjd662Controller;
//var_dump($_REQUEST);
?>
