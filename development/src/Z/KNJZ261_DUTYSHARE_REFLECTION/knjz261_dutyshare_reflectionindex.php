<?php

require_once('for_php7.php');

require_once('knjz261_dutyshare_reflectionModel.inc');
require_once('knjz261_dutyshare_reflectionQuery.inc');

class knjz261_dutyshare_reflectionController extends Controller {
    var $ModelClassName = "knjz261_dutyshare_reflectionModel";
    var $ProgramID      = "KNJZ261A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ261_DUTYSHARE_REFLECTION");
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ261_DUTYSHARE_REFLECTION");
                    $this->callView("knjz261_dutyshare_reflectionForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz261_dutyshare_reflectionCtl = new knjz261_dutyshare_reflectionController;
?>
