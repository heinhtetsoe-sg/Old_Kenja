<?php

require_once('for_php7.php');

require_once('knjc033kModel.inc');
require_once('knjc033kQuery.inc');

class knjc033kController extends Controller {
    var $ModelClassName = "knjc033kModel";
    var $ProgramID      = "KNJC033K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "chaircd":
                case "change":
                case "course":
                case "kekka_syubetu":
                case "subclasscd":
                case "reset":
                case "back":
                    $this->callView("knjc033kForm1");
                    break 2;
                case "replace":
                    $this->callView("knjc033kSubForm2");
                    break 2;
                case "replace_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModelSub();
                    $sessionInstance->setCmd("replace");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "newpopup":
                    $this->callView("knjc033kSubForm");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc033kCtl = new knjc033kController;
?>
