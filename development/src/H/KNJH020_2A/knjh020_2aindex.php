<?php

require_once('for_php7.php');

require_once('knjh020_2aModel.inc');
require_once('knjh020_2aQuery.inc');

class knjh020_2aController extends Controller {
    var $ModelClassName = "knjh020_2aModel";
    var $ProgramID        = "KNJH020A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", "KNJH020_2A");
                    $this->callView("knjh020_2aForm2");
                    break 2;
                case "main":
                    $this->callView("knjh020_2aForm1");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", "KNJH020_2A");
                    if (!$sessionInstance->auth){
                        $this->checkAuth(DEF_UPDATE_RESTRICT);
                    }
                    $sessionInstance->getInsertModel();
                    $this->callView("knjh020_2aForm2");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJH020_2A");
                    if (!$sessionInstance->auth){
                        $this->checkAuth(DEF_UPDATE_RESTRICT);
                    }
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjh020_2aForm2");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", "KNJH020_2A");
                    if (!$sessionInstance->auth){
                        $this->checkAuth(DEF_UPDATE_RESTRICT);
                    }
                    $sessionInstance->getDeleteModel();
                    $this->callView("knjh020_2aForm2");
                    break 2;
                case "apply":
                    $sessionInstance->setAccessLogDetail("S", "KNJH020_2A");
                    $sessionInstance->getApplyModel();
                    $this->callView("knjh020_2aForm2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $args["right_src"] = "knjh020_2aindex.php?cmd=main";
                    $args["edit_src"] = "knjh020_2aindex.php?cmd=edit";
                    $args["rows"] = "30%,*%";
                    View::frame($args, "frame3.html");
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjh020_2aCtl = new knjh020_2aController;
?>
