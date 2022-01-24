<?php

require_once('for_php7.php');

require_once('knjl080gModel.inc');
require_once('knjl080gQuery.inc');

class knjl080gController extends Controller {
    var $ModelClassName = "knjl080gModel";
    var $ProgramID      = "KNJL080G";
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
                    $this->callView("knjl080gForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl080gCtl = new knjl080gController;
//var_dump($_REQUEST);
?>
