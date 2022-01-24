<?php
require_once('knjl349kModel.inc');
require_once('knjl349kQuery.inc');

class knjl349kController extends Controller {
    var $ModelClassName = "knjl349kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl349k":
                    $sessionInstance->knjl349kModel();
                    $this->callView("knjl349kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl349kCtl = new knjl349kController;
var_dump($_REQUEST);
?>
