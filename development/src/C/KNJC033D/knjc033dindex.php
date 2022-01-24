<?php

require_once('for_php7.php');

require_once('knjc033dModel.inc');
require_once('knjc033dQuery.inc');

class knjc033dController extends Controller {
    var $ModelClassName = "knjc033dModel";
    var $ProgramID      = "KNJC033D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "KNJC032D":
                case "knjc033d":
                case "clear":
                case "main":
                    $sessionInstance->knjc033dModel();      //コントロールマスタの呼び出し
                    $this->callView("knjc033dForm1");
                    exit;
                case "form2":
                case "form2_chg":
                case "form2_clear":
                    $this->callView("knjc033dForm2");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjc033d");
                    break 1;
                case "form2_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel2();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "form2_delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel2();
                    $sessionInstance->setCmd("form2");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc033dCtl = new knjc033dController;
?>
