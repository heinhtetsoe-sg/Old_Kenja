<?php

require_once('for_php7.php');

require_once('knjz286_position_reflectionModel.inc');
require_once('knjz286_position_reflectionQuery.inc');

class knjz286_position_reflectionController extends Controller {
    var $ModelClassName = "knjz286_position_reflectionModel";
    var $ProgramID      = "KNJZ286A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ286_POSITION_REFLECTION");
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ286_POSITION_REFLECTION");
                    $this->callView("knjz286_position_reflectionForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz286_position_reflectionCtl = new knjz286_position_reflectionController;
?>
