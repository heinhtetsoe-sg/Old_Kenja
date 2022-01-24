<?php

require_once('for_php7.php');

require_once('knjg042Model.inc');
require_once('knjg042Query.inc');

class knjg042Controller extends Controller {
    var $ModelClassName = "knjg042Model";
    var $ProgramID      = "KNJG042";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "init":
                case "list":
                    $this->callView("knjg042Form1");
                    break 2;
                case "edit":
                case "reset":
                    $this->callView("knjg042Form2");
                    break 2;
                case "update0":
                case "update1":
                case "update2":
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
                    $args["left_src"] = "knjg042index.php?cmd=list";
                    $args["right_src"] = "knjg042index.php?cmd=edit";
                    $args["cols"] = "42%,*%";
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
$knjg042Ctl = new knjg042Controller;
?>
