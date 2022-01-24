<?php

require_once('for_php7.php');

require_once('knjj183_familyModel.inc');
require_once('knjj183_familyQuery.inc');

class knjj183_familyController extends Controller {
    var $ModelClassName = "knjj183_familyModel";
    var $ProgramID        = "KNJJ183";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                    $this->callView("knjj183_familyForm1");
                    break 2;
                case "edit":
                case "clear":
                    $this->callView("knjj183_familyForm2");
                    break 2;
                case "apply":
                    $sessionInstance->setAccessLogDetail("S", "KNJJ183_FAMILY");
                    $sessionInstance->getApplyModel();
                    $this->callView("knjj183_familyForm2");
                    break 2;
                case "addFamily":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "updFamily":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delFamily":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "":
                    $args["right_src"] = "knjj183_familyindex.php?cmd=list";
                    $args["edit_src"] = "knjj183_familyindex.php?cmd=edit";
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
$knjj183_familyCtl = new knjj183_familyController;
?>
