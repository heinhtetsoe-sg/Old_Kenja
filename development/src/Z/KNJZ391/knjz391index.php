<?php

require_once('for_php7.php');

require_once('knjz391Model.inc');
require_once('knjz391Query.inc');

class knjz391Controller extends Controller {
    var $ModelClassName = "knjz391Model";
    var $ProgramID      = "KNJZ391";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "check":
                case "clear":
                    $this->callView("knjz391Form2");
                    break 2;
                case "list":
                case "combo":
                    $this->callView("knjz391Form1");
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
                    $args["left_src"]  = "knjz391index.php?cmd=list";
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
$knjz391Ctl = new knjz391Controller;
?>
