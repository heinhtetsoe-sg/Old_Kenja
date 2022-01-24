<?php

require_once('for_php7.php');

require_once('knjl021kModel.inc');
require_once('knjl021kQuery.inc');

class knjl021kController extends Controller {
    var $ModelClassName = "knjl021kModel";
    var $ProgramID      = "KNJL021K";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
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
                    $this->callView("knjl021kForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl021kCtl = new knjl021kController;
//var_dump($_REQUEST);
?>
