<?php

require_once('for_php7.php');

require_once('knjz072_subclass_reflectionModel.inc');
require_once('knjz072_subclass_reflectionQuery.inc');

class knjz072_subclass_reflectionController extends Controller {
    var $ModelClassName = "knjz072_subclass_reflectionModel";
    var $ProgramID      = "KNJZ072A";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ072_SUBCLASS_REFLECTION");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel":
                case "clear";
                case "btn_def";
                    $sessionInstance->setAccessLogDetail("S", "KNJZ072_SUBCLASS_REFLECTION");
                    $this->callView("knjz072_subclass_reflectionForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz072_subclass_reflectionCtl = new knjz072_subclass_reflectionController;
//var_dump($_REQUEST);
?>
