<?php
require_once('knjl356kModel.inc');
require_once('knjl356kQuery.inc');

class knjl356kController extends Controller {
    var $ModelClassName = "knjl356kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl356k":
                    $sessionInstance->knjl356kModel();
                    $this->callView("knjl356kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl356kCtl = new knjl356kController;
var_dump($_REQUEST);
?>
