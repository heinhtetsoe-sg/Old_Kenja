<?php

require_once('for_php7.php');

require_once('knjz069aModel.inc');
require_once('knjz069aQuery.inc');

class knjz069aController extends Controller {
    var $ModelClassName = "knjz069aModel";
    var $ProgramID      = "KNJZ069A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "updEdit":
                case "clear":
                    $this->callView("knjz069aForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->knjz069aModel();        //コントロールマスタの呼び出し
                    $this->callView("knjz069aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz069aCtl = new knjz069aController;
?>
