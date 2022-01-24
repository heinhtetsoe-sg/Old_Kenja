<?php

require_once('for_php7.php');

require_once('knjd425mModel.inc');
require_once('knjd425mQuery.inc');

class knjd425mController extends Controller {
    var $ModelClassName = "knjd425mModel";
    var $ProgramID      = "knjd425m";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "chgSchKind":
                case "chgSeme":
                case "chgSub":
                case "chgChair":
                case "clear":
                    $sessionInstance->knjd425mModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd425mForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd425mForm1", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->knjd425mModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd425mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd425mCtl = new knjd425mController;
?>
