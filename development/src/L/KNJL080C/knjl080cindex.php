<?php

require_once('for_php7.php');

require_once('knjl080cModel.inc');
require_once('knjl080cQuery.inc');

class knjl080cController extends Controller {
    var $ModelClassName = "knjl080cModel";
    var $ProgramID      = "KNJL080C";
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
                case "csv":
                    $sessionInstance->CSVModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;                
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "main":
                case "clear";
                    $this->callView("knjl080cForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl080cCtl = new knjl080cController;
//var_dump($_REQUEST);
?>
