<?php

require_once('for_php7.php');

require_once('knji091Model.inc');
require_once('knji091Query.inc');

class knji091Controller extends Controller {
    var $ModelClassName = "knji091Model";
    var $ProgramID      = "KNJI091";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knji091":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knji091Model();       //コントロールマスタの呼び出し
                    $this->callView("knji091Form1");
                    exit;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knji091");
                    break 1;
                case "reset":
                    $this->callView("knji091Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knji091Ctl = new knji091Controller;
//var_dump($_REQUEST);
?>
