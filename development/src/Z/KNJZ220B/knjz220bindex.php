<?php

require_once('for_php7.php');

require_once('knjz220bModel.inc');
require_once('knjz220bQuery.inc');

class knjz220bController extends Controller {
    var $ModelClassName = "knjz220bModel";
    var $ProgramID      = "KNJZ220B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "reset":
                case "changeClass":
                case "changeSub":
                    $this->callView("knjz220bForm1");
                    exit;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
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
$knjz220bCtl = new knjz220bController;
var_dump($_REQUEST);
?>
