<?php

require_once('for_php7.php');

require_once('knjz051_major_reflectionModel.inc');
require_once('knjz051_major_reflectionQuery.inc');

class knjz051_major_reflectionController extends Controller {
    var $ModelClassName = "knjz051_major_reflectionModel";
    var $ProgramID      = "KNJZ051A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ051_MAJOR_REFLECTION");
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ051_MAJOR_REFLECTION");
                    $this->callView("knjz051_major_reflectionForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz051_major_reflectionCtl = new knjz051_major_reflectionController;
?>
