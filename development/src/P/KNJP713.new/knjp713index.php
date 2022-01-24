<?php

require_once('for_php7.php');

require_once('knjp713Model.inc');
require_once('knjp713Query.inc');

class knjp713Controller extends Controller {
    var $ModelClassName = "knjp713Model";
    var $ProgramID      = "KNJP713";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                case "change2":
                case "reload":
                case "grpChange":
                    $this->callView("knjp713Form2");
                    break 2;
                case "list":
                case "changeYear":
                case "change":
                    $this->callView("knjp713Form1");
                    break 2;
                case "add":
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $sessionInstance->boot_flg = true;
                    $args["left_src"] = "knjp713index.php?cmd=list";
                    $args["right_src"] = "knjp713index.php?cmd=edit";
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
$knjp713Ctl = new knjp713Controller;
?>
