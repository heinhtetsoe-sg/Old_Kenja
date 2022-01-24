<?php

require_once('for_php7.php');

require_once('knjl580fModel.inc');
require_once('knjl580fQuery.inc');

class knjl580fController extends Controller {
    var $ModelClassName = "knjl580fModel";
    var $ProgramID      = "KNJL580F";
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
                case "changApp":
                case "clear";
                    $this->callView("knjl580fForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl580fCtl = new knjl580fController;
//var_dump($_REQUEST);
?>
