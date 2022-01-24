<?php

require_once('for_php7.php');

require_once('knjl306dModel.inc');
require_once('knjl306dQuery.inc');

class knjl306dController extends Controller {
    var $ModelClassName = "knjl306dModel";
    var $ProgramID      = "KNJL306D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "kakutei":
                case "change":
                case "main":
                case "reset":
                    $this->callView("knjl306dForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("kakutei");
                    break 1;
                case "copy":
                    $sessionInstance->getCopyYearModel();
                    $sessionInstance->setCmd("main");
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
$knjl306dCtl = new knjl306dController;
?>
