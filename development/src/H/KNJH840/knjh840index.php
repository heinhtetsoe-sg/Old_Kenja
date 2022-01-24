<?php
require_once('knjh840Model.inc');
require_once('knjh840Query.inc');

class knjh840Controller extends Controller {
    var $ModelClassName = "knjh840Model";
    var $ProgramID      = "KNJH840";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $this->callView("knjh840Form1");
                    exit;

                case "edit":
                case "change":
                case "hyouzi":
                case "kubun_change":
                case "kaisu_change":
                
                case "subclasscd":
                case "chaircd":
                    $this->callView("knjh840Form1");
                    break 2;
                case "csv":
                    if(!$sessionInstance->getCsvModel()){
                        $this->callView("knjh840Form1");
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
$KNJh840Ctl = new knjh840Controller;
//var_dump($_REQUEST);
?>
