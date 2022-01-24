<?php

require_once('for_php7.php');

require_once('knjp716Model.inc');
require_once('knjp716Query.inc');

class knjp716Controller extends Controller {
    var $ModelClassName = "knjp716Model";
    var $ProgramID      = "knjp716";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                case "change2":
                case "reload":
                    $this->callView("knjp716Form2");
                    break 2;
                case "list":
                case "change":
                    $this->callView("knjp716Form1");
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
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $sessionInstance->boot_flg = true;
                    $args["left_src"] = "knjp716index.php?cmd=list";
                    $args["right_src"] = "knjp716index.php?cmd=edit";
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
$knjp716Ctl = new knjp716Controller;
?>
