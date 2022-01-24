<?php

require_once('for_php7.php');

require_once('knjl083pModel.inc');
require_once('knjl083pQuery.inc');

class knjl083pController extends Controller {
    var $ModelClassName = "knjl083pModel";
    var $ProgramID      = "KNJL083P";
    function main() {
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
                    $this->callView("knjl083pForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl083pCtl = new knjl083pController;
?>
