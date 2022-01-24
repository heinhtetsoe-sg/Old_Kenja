<?php
require_once('knjh805Model.inc');
require_once('knjh805Query.inc');

class knjh805Controller extends Controller {
    var $ModelClassName = "knjh805Model";
    var $ProgramID      = "KNJH805";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $this->callView("knjh805Form1");
                    exit;

                case "edit":
                case "change":
                case "hyouzi":
                case "clear":
                
                case "subclasscd":
                case "chaircd":
                    $this->callView("knjh805Form1");
                    break 2;
                case "csv":
                    if(!$sessionInstance->getCsvModel()){
                        $this->callView("knjh805Form1");
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
$KNJh805Ctl = new knjh805Controller;
//var_dump($_REQUEST);
?>
