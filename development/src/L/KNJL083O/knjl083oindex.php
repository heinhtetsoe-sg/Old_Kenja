<?php

require_once('for_php7.php');

require_once('knjl083oModel.inc');
require_once('knjl083oQuery.inc');

class knjl083oController extends Controller {
    var $ModelClassName = "knjl083oModel";
    var $ProgramID      = "KNJL083O";
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
                case "classKakutei":
                    $this->callView("knjl083oKakutei");
                    break 2;
                case "updateClass":
                    $sessionInstance->getClassUpdateModel();
                    $sessionInstance->setCmd("classKakutei");
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
                    $this->callView("knjl083oForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl083oCtl = new knjl083oController;
//var_dump($_REQUEST);
?>
