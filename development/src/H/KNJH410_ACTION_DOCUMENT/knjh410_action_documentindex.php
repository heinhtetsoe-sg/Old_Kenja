<?php
require_once('knjh410_action_documentModel.inc');
require_once('knjh410_action_documentQuery.inc');

class knjh410_action_documentController extends Controller {
    var $ModelClassName = "knjh410_action_documentModel";
    var $ProgramID      = "KNJH410_ACTION_DOCUMENT";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "insertSub":
                case "updateSub":
                case "reset":
                    $this->callView("knjh410_action_documentForm1");
                    break 2;
                case "update":
                case "insert":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "", $sessionInstance->sendAuth);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("reset");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJh400Ctl = new knjh410_action_documentController;
//var_dump($_REQUEST);
?>
