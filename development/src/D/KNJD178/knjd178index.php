<?php

require_once('for_php7.php');

require_once('knjd178Model.inc');
require_once('knjd178Query.inc');

class knjd178Controller extends Controller {
    var $ModelClassName = "knjd178Model";
    var $ProgramID      = "KNJD178";

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
                case "clear";
                    $sessionInstance->knjd178Model();
                    $this->callView("knjd178Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd178Ctl = new knjd178Controller;
//var_dump($_REQUEST);
?>
