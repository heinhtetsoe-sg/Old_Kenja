<?php

require_once('for_php7.php');

require_once('knjm240Model.inc');
require_once('knjm240Query.inc');

class knjm240Controller extends Controller {
    var $ModelClassName = "knjm240Model";
    var $ProgramID      = "knjm240";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "init":
                case "list":
                    $this->callView("knjm240Form1");
                    break 2;
                case "edit":
                case "reset":
                    $this->callView("knjm240Form2");
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
                    $args["left_src"] = "knjm240index.php?cmd=list";
                    $args["right_src"] = "knjm240index.php?cmd=edit";
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
$knjm240Ctl = new knjm240Controller;
?>
