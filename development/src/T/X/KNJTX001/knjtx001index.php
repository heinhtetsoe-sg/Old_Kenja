<?php

require_once('for_php7.php');

require_once('knjtx001Model.inc');
require_once('knjtx001Query.inc');

class knjtx001Controller extends Controller {
    var $ModelClassName = "knjtx001Model";
    var $ProgramID      = "KNJTX001";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "copy":
                    $this->callView("knjtx001Form1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 1;
                case "":
                      $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjtx001Ctl = new knjtx001Controller;
//var_dump($_REQUEST);
?>
