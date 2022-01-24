<?php

require_once('for_php7.php');

require_once('knjz072aModel.inc');
require_once('knjz072aQuery.inc');

class knjz072aController extends Controller {
    var $ModelClassName = "knjz072aModel";
    var $ProgramID      = "KNJZ072A";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
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
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz072aForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz072aCtl = new knjz072aController;
//var_dump($_REQUEST);
?>
