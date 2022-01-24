<?php

require_once('for_php7.php');

require_once('knjs570Model.inc');
require_once('knjs570Query.inc');

class knjs570Controller extends Controller {
    var $ModelClassName = "knjs570Model";
    var $ProgramID      = "KNJS570";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
               case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "main":
                    $this->callView("knjs570Form1");
                    break 2;
                case "":
                    $this->callView("knjs570Form1");
                    break 2;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "knjs570":
                    $sessionInstance->knjs570Model();
                    $this->callView("knjs570Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjs570Ctl = new knjs570Controller;
//var_dump($_REQUEST);
?>
