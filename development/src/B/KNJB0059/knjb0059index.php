<?php

require_once('for_php7.php');

require_once('knjb0059Model.inc');
require_once('knjb0059Query.inc');

class knjb0059Controller extends Controller {
    var $ModelClassName = "knjb0059Model";
    var $ProgramID      = "KNJB0059";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                case "clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjb0059Form2");
                    break 2;
                case "list":
                case "combo":
                    $this->callView("knjb0059Form1");
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
                    $args["left_src"]   = "knjb0059index.php?cmd=list";
                    $args["right_src"]  = "knjb0059index.php?cmd=edit";
                    $args["cols"] = "39%,61%";
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
$knjb0059Ctl = new knjb0059Controller;
?>
