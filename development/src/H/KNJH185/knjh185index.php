<?php

require_once('for_php7.php');

require_once('knjh185Model.inc');
require_once('knjh185Query.inc');

class knjh185Controller extends Controller {
    var $ModelClassName = "knjh185Model";
    var $ProgramID      = "KNJH185";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knjh185":
                case "reset":
                    $sessionInstance->knjh185Model();      //コントロールマスタの呼び出し
                    $this->callView("knjh185Form1");
                    exit;
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjh185");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh185Ctl = new knjh185Controller;
?>
