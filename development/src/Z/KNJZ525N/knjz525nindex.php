<?php

require_once('for_php7.php');

require_once('knjz525nModel.inc');
require_once('knjz525nQuery.inc');

class knjz525nController extends Controller {
    var $ModelClassName = "knjz525nModel";
    var $ProgramID      = "KNJZ525N";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
               case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "error":
                    $this->callView("error");
                    break 2;
               case "kakutei":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz525nForm1");
                    break 2;
               case "clear":
               case "main":
               case "chgPtrn":
               case "":
                    $this->callView("knjz525nForm1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz525nCtl = new knjz525nController;
?>
