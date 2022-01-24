<?php

require_once('for_php7.php');

require_once('knjz062_class_reflectionModel.inc');
require_once('knjz062_class_reflectionQuery.inc');

class KNJZ062_CLASS_REFLECTIONController extends Controller {
    var $ModelClassName = "knjz062_class_reflectionModel";
    var $ProgramID      = "KNJZ062_CLASS_REFLECTION";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz062_class_reflectionForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz062_class_reflectionCtl = new knjz062_class_reflectionController;
//var_dump($_REQUEST);
?>
