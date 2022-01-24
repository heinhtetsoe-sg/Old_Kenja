<?php

require_once('for_php7.php');

require_once('knjd193Model.inc');
require_once('knjd193Query.inc');

class knjd193Controller extends Controller {
    var $ModelClassName = "knjd193Model";
    var $ProgramID      = "KNJD193";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                case "grade":
                case "clear";
                    $sessionInstance->knjd193Model();
                    $this->callView("knjd193Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd193Ctl = new knjd193Controller;
//var_dump($_REQUEST);
?>
