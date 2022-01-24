<?php

require_once('for_php7.php');

require_once('knjd410Model.inc');
require_once('knjd410Query.inc');

class knjd410Controller extends Controller {
    var $ModelClassName = "knjd410Model";
    var $ProgramID      = "KNJD410";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "clear":
                    $this->callView("knjd410Form2");
                    break 2;
                case "list":
                case "combo":
                    $this->callView("knjd410Form1");
                    break 2;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
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
                    $args["left_src"] = "knjd410index.php?cmd=list";
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
$knjd410Ctl = new knjd410Controller;
?>
