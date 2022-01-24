<?php

require_once('for_php7.php');

require_once('knjd419Model.inc');
require_once('knjd419Query.inc');

class knjd419Controller extends Controller {
    var $ModelClassName = "knjd419Model";
    var $ProgramID      = "KNJD419";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "clear":
                    $this->callView("knjd419Form2");
                    break 2;
                case "list":
                case "combo":
                    $this->callView("knjd419Form1");
                    break 2;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "update":
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
                    $args["left_src"]  = "knjd419index.php?cmd=list";
                    $args["right_src"] = "";
                    $args["cols"] = "50%,50%";
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
$knjd419Ctl = new knjd419Controller;
?>
