<?php

require_once('for_php7.php');

require_once('knjd420qModel.inc');
require_once('knjd420qQuery.inc');

class knjd420qController extends Controller {
    var $ModelClassName = "knjd420qModel";
    var $ProgramID      = "knjd420q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list_set":
                case "sort":
                case "edit":
                case "set":
                case "chgSub":
                case "chgChair":
                case "clear":
                    $sessionInstance->knjd420qModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd420qForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd420qForm1", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd420qForm1", $sessionInstance->auth);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->knjd420qModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd420qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd420qCtl = new knjd420qController;
?>
