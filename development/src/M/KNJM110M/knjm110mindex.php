<?php

require_once('for_php7.php');

require_once('knjm110mModel.inc');
require_once('knjm110mQuery.inc');

class knjm110mController extends Controller {
    var $ModelClassName = "knjm110mModel";
    var $ProgramID      = "KNJM110M";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "init":
                case "list":
                    $this->callView("knjm110mForm1");
                    break 2;
                case "kch":
                    $this->callView("knjm110mForm1");
                    $sessionInstance->setCmd("edit");
                    break 2;
                case "edit":
                case "edit2":
                case "reset":
                    $this->callView("knjm110mForm2");
                    break 2;
                case "update":
                case "delete":
                case "add":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjm110mindex.php?cmd=list";
                    $args["right_src"] = "knjm110mindex.php?cmd=edit";
                    $args["cols"] = "50%,*%";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm110mCtl = new knjm110mController;
?>
