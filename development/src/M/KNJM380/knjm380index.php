<?php

require_once('for_php7.php');

require_once('knjm380Model.inc');
require_once('knjm380Query.inc');

class knjm380Controller extends Controller {
    var $ModelClassName = "knjm380Model";
    var $ProgramID      = "knjm380";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "init":
                case "list":
                    $this->callView("knjm380Form1");
                    break 2;
                case "edit":
                case "reset":
                    $this->callView("knjm380Form2");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
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
                    $args["left_src"] = "knjm380index.php?cmd=list";
                    $args["right_src"] = "knjm380index.php?cmd=edit";
                    $args["cols"] = "55%,*%";
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
$knjm380Ctl = new knjm380Controller;
?>
