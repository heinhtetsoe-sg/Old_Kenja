<?php

require_once('for_php7.php');

require_once('knjd132tModel.inc');
require_once('knjd132tQuery.inc');

class knjd132tController extends Controller {
    var $ModelClassName = "knjd132tModel";
    var $ProgramID      = "KNJD132T";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "cmdStart":
                case "main":
                case "reset":
                    $this->callView("knjd132tForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("cmdStart");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd132tCtl = new knjd132tController;
?>
