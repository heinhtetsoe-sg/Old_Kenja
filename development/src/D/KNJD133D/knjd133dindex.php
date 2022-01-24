<?php

require_once('for_php7.php');

require_once('knjd133dModel.inc');
require_once('knjd133dQuery.inc');

class knjd133dController extends Controller {
    var $ModelClassName = "knjd133dModel";
    var $ProgramID      = "KNJD133D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "csvInputMain":
                case "subclasscd":
                case "reset":
                case "edit":
                    $this->callView("knjd133dForm1");
                    break 2;
                case "chaircd":
                    $this->callView("knjd133dForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }

        }
    }
}
$knjd133dCtl = new knjd133dController;
?>
