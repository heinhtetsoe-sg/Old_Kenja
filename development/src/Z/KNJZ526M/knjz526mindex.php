<?php

require_once('for_php7.php');

require_once('knjz526mModel.inc');
require_once('knjz526mQuery.inc');

class knjz526mController extends Controller {
    var $ModelClassName = "knjz526mModel";
    var $ProgramID      = "KNJZ526M";

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
               case "kakutei":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz526mForm1");
                    break 2;
               case "clear":
               case "change":
               case "main":
               case "":
                    $this->callView("knjz526mForm1");
                    break 2;
               case "error":
                    $this->callView("error");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz526mCtl = new knjz526mController;
?>
