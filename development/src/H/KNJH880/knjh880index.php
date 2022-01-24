<?php
require_once('knjh880Model.inc');
require_once('knjh880Query.inc');

class knjh880Controller extends Controller {
    var $ModelClassName = "knjh880Model";
    var $ProgramID      = "KNJH880";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $this->callView("knjh880Form1");
                    exit;

                case "edit":
                case "change":
                case "hyouzi":
                case "clear":
                case "order":
                
                case "subclasscd":
                case "chaircd":
                    $this->callView("knjh880Form1");
                    break 2;
                case "csv":
                    if(!$sessionInstance->getCsvModel()){
                        $this->callView("knjh880Form1");
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
$KNJh880Ctl = new knjh880Controller;
//var_dump($_REQUEST);
?>
