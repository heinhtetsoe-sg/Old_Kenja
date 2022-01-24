<?php
require_once('knjl324kModel.inc');
require_once('knjl324kQuery.inc');

class knjl324kController extends Controller {
    var $ModelClassName = "knjl324kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl324k":
                    $sessionInstance->knjl324kModel();
                    $this->callView("knjl324kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl324kCtl = new knjl324kController;
var_dump($_REQUEST);
?>
