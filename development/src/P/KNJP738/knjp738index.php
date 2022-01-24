<?php

require_once('for_php7.php');

require_once('knjp738Model.inc');
require_once('knjp738Query.inc');

class knjp738Controller extends Controller {
    var $ModelClassName = "knjp738Model";
    var $ProgramID      = "KNJP738";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "clear":
                case "editNew":
                    $this->callView("knjp738Form2");
                    break 2;
                case "list":
                case "change":
                    $this->callView("knjp738Form1");
                    break 2;
                case "add":
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $sessionInstance->boot_flg = true;
                    $args["left_src"]   = "knjp738index.php?cmd=list";
                    $args["right_src"]  = "knjp738index.php?cmd=edit";
                    $args["cols"]       = "45%,55%";
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
$knjp738Ctl = new knjp738Controller;
?>
