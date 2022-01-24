<?php

require_once('for_php7.php');


require_once('knjd129bModel.inc');
require_once('knjd129bQuery.inc');

class knjd129bController extends Controller {
    var $ModelClassName = "knjd129bModel";
    var $ProgramID      = "KNJD129B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "calculate":
                    $this->callView("knjd129bForm1");
                   break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
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
$knjd129bCtl = new knjd129bController;
?>
