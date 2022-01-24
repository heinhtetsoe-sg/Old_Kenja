<?php

require_once('for_php7.php');

require_once('knjd414Model.inc');
require_once('knjd414Query.inc');

class knjd414Controller extends Controller {
    var $ModelClassName = "knjd414Model";
    var $ProgramID      = "KNJD414";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "clear":
                case "combo2":
                case "button_after":
                case "delete_after":
                case "read":
                    $this->callView("knjd414Form2");
                    break 2;
                case "list":
                case "combo":
                    $this->callView("knjd414Form1");
                    break 2;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "add":
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("button_after");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("button_after");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("delete_after");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjd414index.php?cmd=list";
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
$knjd414Ctl = new knjd414Controller;
?>
