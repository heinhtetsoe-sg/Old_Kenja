<?php

require_once('for_php7.php');

require_once('knjz290aModel.inc');
require_once('knjz290aQuery.inc');

class knjz290aController extends Controller {
    var $ModelClassName = "knjz290aModel";
    var $ProgramID      = "KNJZ290A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel";
                    $this->callView("knjz290aForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz290aCtl = new knjz290aController;
//var_dump($_REQUEST);
?>
