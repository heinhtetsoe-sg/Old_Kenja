<?php

require_once('for_php7.php');

require_once('knjz064aModel.inc');
require_once('knjz064aQuery.inc');

class knjz064aController extends Controller {
    var $ModelClassName = "knjz064aModel";
    var $ProgramID      = "KNJZ064A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "clear":
                    $this->callView("knjz064aForm2");
                    break 2;
                case "list":
                case "combo":
                    $this->callView("knjz064aForm1");
                    break 2;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update2":
                    $sessionInstance->getUpdateModel2();
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
                    $args["left_src"] = "knjz064aindex.php?cmd=list";
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
$knjz064aCtl = new knjz064aController;
?>
