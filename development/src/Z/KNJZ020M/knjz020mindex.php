<?php

require_once('for_php7.php');

require_once('knjz020mModel.inc');
require_once('knjz020mQuery.inc');

class knjz020mController extends Controller {
    var $ModelClassName = "knjz020mModel";
    var $ProgramID      = "KNJZ020M";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "":
                case "edit":
                case "reset":
                    $this->callView("knjz020mForm1");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
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
$knjz020mCtl = new knjz020mController;
//var_dump($_REQUEST);
?>
