<?php

require_once('for_php7.php');

require_once('knjh110a_2Model.inc');
require_once('knjh110a_2Query.inc');

class knjh110a_2Controller extends Controller {
    var $ModelClassName = "knjh110a_2Model";
    var $ProgramID      = "KNJH110A_2";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "cmb_sub";
                case "link";
                case "main";
                    $this->callView("knjh110a_2Form1");
                    break 2;
                case "add":
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
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
$knjh110a_2Ctl = new knjh110a_2Controller;
//var_dump($_REQUEST);
?>
