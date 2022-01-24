<?php

require_once('for_php7.php');

require_once('knjz065mModel.inc');
require_once('knjz065mQuery.inc');

class knjz065mController extends Controller {
    var $ModelClassName = "knjz065mModel";
    var $ProgramID      = "KNJZ065M";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "clear":
                    $this->callView("knjz065mForm2");
                    break 2;
                case "list":
                case "changeKind":
                    $this->callView("knjz065mForm1");
                    break 2;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "update":
                case "insert":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
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
                    //分割フレーム作成
                    $args["left_src"] = "knjz065mindex.php?cmd=list";
                    $args["right_src"] = "";
                    $args["cols"] = "40%,60%";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz065mCtl = new knjz065mController;
?>
