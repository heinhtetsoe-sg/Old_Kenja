<?php
require_once('knjh810Model.inc');
require_once('knjh810Query.inc');

class knjh810Controller extends Controller {
    var $ModelClassName = "knjh810Model";
    var $ProgramID      = "KNJH810";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $this->callView("knjh810Form1");
                    exit;

                case "edit":
                case "change":
                case "hyouzi":
                case "kubun_change":
                case "kaisu_change":
                
                case "subclasscd":
                case "chaircd":
                    $this->callView("knjh810Form1");
                    break 2;
                case "csv":
                    if(!$sessionInstance->getCsvModel()){
                        $this->callView("knjh810Form1");
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
$KNJh810Ctl = new knjh810Controller;
//var_dump($_REQUEST);
?>
