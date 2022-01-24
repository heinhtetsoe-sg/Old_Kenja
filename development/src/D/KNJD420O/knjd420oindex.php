<?php

require_once('for_php7.php');

require_once('knjd420oModel.inc');
require_once('knjd420oQuery.inc');

class knjd420oController extends Controller {
    var $ModelClassName = "knjd420oModel";
    var $ProgramID      = "knjd420o";

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
                    $sessionInstance->knjd420oModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd420oForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd420oForm1", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd420oForm1", $sessionInstance->auth);
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
                    $sessionInstance->knjd420oModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd420oForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd420oCtl = new knjd420oController;
?>
