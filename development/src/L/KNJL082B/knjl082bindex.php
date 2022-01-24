<?php

require_once('for_php7.php');

require_once('knjl082bModel.inc');
require_once('knjl082bQuery.inc');

class knjl082bController extends Controller {
    var $ModelClassName = "knjl082bModel";
    var $ProgramID      = "KNJL082B";
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
                case "change":
                case "main":
                case "clear";
                    $this->callView("knjl082bForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl082bCtl = new knjl082bController;
//var_dump($_REQUEST);
?>
