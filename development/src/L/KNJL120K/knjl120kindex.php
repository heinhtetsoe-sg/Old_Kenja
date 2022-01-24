<?php

require_once('for_php7.php');

require_once('knjl120kModel.inc');
require_once('knjl120kQuery.inc');

class knjl120kController extends Controller {
    var $ModelClassName = "knjl120kModel";
    var $ProgramID      = "KNJL120K";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "upload":
                    $sessionInstance->getUploadModel();
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
                    $this->callView("knjl120kForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl120kCtl = new knjl120kController;
//var_dump($_REQUEST);
?>
