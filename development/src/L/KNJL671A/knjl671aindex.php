<?php

require_once('for_php7.php');

require_once('knjl671aModel.inc');
require_once('knjl671aQuery.inc');

class knjl671aController extends Controller {
    var $ModelClassName = "knjl671aModel";
    var $ProgramID      = "KNJL671A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "sort":
                case "clear":
                case "search":
                    $sessionInstance->knjl671aModel();
                    $this->callView("knjl671aForm1");
                    exit;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl671aCtl = new knjl671aController;
?>
