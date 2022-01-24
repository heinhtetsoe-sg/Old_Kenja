<?php

require_once('for_php7.php');

require_once('knjd418Model.inc');
require_once('knjd418Query.inc');

class knjd418Controller extends Controller {
    var $ModelClassName = "knjd418Model";
    var $ProgramID      = "KNJD418";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "set":
                case "check":
                case "clear":
                    $this->callView("knjd418Form2");
                    break 2;
                case "list":
                case "combo":
                    $this->callView("knjd418Form1");
                    break 2;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
               case "update":
                    $sessionInstance->getUpdateModel();
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
                    $args["left_src"]  = "knjd418index.php?cmd=list";
                    $args["right_src"] = "";
                    $args["cols"] = "45%,55%";
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
$knjd418Ctl = new knjd418Controller;
?>
