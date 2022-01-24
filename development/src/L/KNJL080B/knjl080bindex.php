<?php

require_once('for_php7.php');

require_once('knjl080bModel.inc');
require_once('knjl080bQuery.inc');

class knjl080bController extends Controller {
    var $ModelClassName = "knjl080bModel";
    var $ProgramID      = "KNJL080B";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "uproad":
                    $sessionInstance->getUproadModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "main":
                case "clear";
                    $this->callView("knjl080bForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl080bCtl = new knjl080bController;
//var_dump($_REQUEST);
?>
