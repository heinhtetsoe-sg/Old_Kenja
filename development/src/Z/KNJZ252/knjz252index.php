<?php

require_once('for_php7.php');

require_once('knjz252Model.inc');
require_once('knjz252Query.inc');

class knjz252Controller extends Controller {
    var $ModelClassName = "knjz252Model";
    var $ProgramID      = "KNJZ252";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "init":
                case "list":
                    $this->callView("knjz252Form1");
                    break 2;
                case "edit":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz252Form2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
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
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz252index.php?cmd=list";
                    $args["right_src"] = "knjz252index.php?cmd=edit";
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

$knjz252Ctl = new knjz252Controller;
//var_dump($_REQUEST);
?>
