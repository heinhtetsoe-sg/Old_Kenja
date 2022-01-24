<?php

require_once('for_php7.php');

require_once('knjl356cModel.inc');
require_once('knjl356cQuery.inc');

class knjl356cController extends Controller {
    var $ModelClassName = "knjl356cModel";
    var $ProgramID      = "KNJL356C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl356c":
                    $sessionInstance->knjl356cModel();
                    $this->callView("knjl356cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl356cCtl = new knjl356cController;
//var_dump($_REQUEST);
?>
