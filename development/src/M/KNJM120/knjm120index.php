<?php

require_once('for_php7.php');

require_once('knjm120Model.inc');
require_once('knjm120Query.inc');

class knjm120Controller extends Controller {
    var $ModelClassName = "knjm120Model";
    var $ProgramID      = "knjm120";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "init":
                case "list":
                    $this->callView("knjm120Form1");
                    break 2;
                case "kch":
                    $this->callView("knjm120Form1");
                    $sessionInstance->setCmd("edit");
                    break 2;
                case "edit":
                case "edit2":
                case "reset":
                    $this->callView("knjm120Form2");
                    break 2;
                case "update":
                case "delete":
                case "add":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjm120index.php?cmd=list";
                    $args["right_src"] = "knjm120index.php?cmd=edit";
                    $args["cols"] = "40%,*%";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm120Ctl = new knjm120Controller;
?>
