<?php

require_once('for_php7.php');

require_once('knjd107Model.inc');
require_once('knjd107Query.inc');

class knjd107Controller extends Controller {
    var $ModelClassName = "knjd107Model";
    var $ProgramID      = "KNJD107";

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
                    $sessionInstance->knjd107Model();
                    $this->callView("knjd107Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd107Ctl = new knjd107Controller;
//var_dump($_REQUEST);
?>
