<?php
require_once('knjl361kModel.inc');
require_once('knjl361kQuery.inc');

class knjl361kController extends Controller {
    var $ModelClassName = "knjl361kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl361k":
                    $sessionInstance->knjl361kModel();
                    $this->callView("knjl361kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl361kCtl = new knjl361kController;
//var_dump($_REQUEST);
?>
