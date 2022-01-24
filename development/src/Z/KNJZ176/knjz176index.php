<?php

require_once('for_php7.php');

require_once('knjz176Model.inc');
require_once('knjz176Query.inc');

class knjz176Controller extends Controller {
    var $ModelClassName = "knjz176Model";
    var $ProgramID      = "KNJZ176";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                case "clear";
                    $sessionInstance->knjz176Model();
                    $this->callView("knjz176Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjz176Ctl = new knjz176Controller;
//var_dump($_REQUEST);
?>
