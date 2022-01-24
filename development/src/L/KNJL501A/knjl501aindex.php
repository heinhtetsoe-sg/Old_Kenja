<?php

require_once('for_php7.php');

require_once('knjl501aModel.inc');
require_once('knjl501aQuery.inc');

class KNJL501AController extends Controller {
    var $ModelClassName = "knjl501aModel";
    var $ProgramID      = "KNJL501A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel";
                    $this->callView("knjl501aForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJL501ACtl = new KNJL501AController;
//var_dump($_REQUEST);
?>
