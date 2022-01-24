<?php

require_once('for_php7.php');

require_once('knjm720Model.inc');
require_once('knjm720Query.inc');

class knjm720Controller extends Controller {
    var $ModelClassName = "knjm720Model";
    var $ProgramID      = "KNJM720";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                case "clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjm720Form2");
                    break 2;
                case "list":
                case "combo":
                    $this->callView("knjm720Form1");
                    break 2;
                case "add":
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
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
                    $args["left_src"]   = "knjm720index.php?cmd=list";
                    $args["right_src"]  = "knjm720index.php?cmd=edit";
                    $args["cols"] = "35%,65%";
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
$knjm720Ctl = new knjm720Controller;
?>
