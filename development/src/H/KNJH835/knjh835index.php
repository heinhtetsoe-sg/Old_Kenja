<?php
require_once('knjh835Model.inc');
require_once('knjh835Query.inc');

class knjh835Controller extends Controller {
    var $ModelClassName = "knjh835Model";
    var $ProgramID      = "KNJH835";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $this->callView("knjh835Form1");
                    exit;

                case "edit":
                case "change":
                case "hyouzi":
                case "clear":
                
                case "subclasscd":
                case "chaircd":
                    $this->callView("knjh835Form1");
                    break 2;
                case "csv":
                    if(!$sessionInstance->getCsvModel()){
                        $this->callView("knjh835Form1");
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
$KNJh835Ctl = new knjh835Controller;
//var_dump($_REQUEST);
?>
