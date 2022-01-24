<?php
require_once('knjh885Model.inc');
require_once('knjh885Query.inc');

class knjh885Controller extends Controller {
    var $ModelClassName = "knjh885Model";
    var $ProgramID      = "KNJH885";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $this->callView("knjh885Form1");
                    exit;

                case "edit":
                case "change":
                case "change_sec":
                case "select":
                    $this->callView("knjh885Form1");
                    break 2;
                    
                case "update":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjh885Form1");
                    break 2;

                case "csv":
                    if(!$sessionInstance->getCsvModel()){
                        $this->callView("knjh885Form1");
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJh885Ctl = new knjh885Controller;
//var_dump($_REQUEST);
?>
