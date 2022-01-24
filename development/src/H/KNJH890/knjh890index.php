<?php
require_once('knjh890Model.inc');
require_once('knjh890Query.inc');

class knjh890Controller extends Controller {
    var $ModelClassName = "knjh890Model";
    var $ProgramID      = "KNJh890";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $this->callView("knjh890Form1");
                    exit;

                case "edit":
                case "change":
                case "select":
                    $this->callView("knjh890Form1");
                    break 2;
                    
                case "update":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjh890Form1");
                    break 2;

                case "csv":
                    if(!$sessionInstance->getCsvModel()){
                        $this->callView("knjh890Form1");
                    }
                    break 2;
                case "import":
                    if(!$sessionInstance->getCsvImportModel()){
                        $this->callView("knjh890Form1");
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
$KNJh890Ctl = new knjh890Controller;
//var_dump($_REQUEST);
?>
