<?php
require_once('knjl342kModel.inc');
require_once('knjl342kQuery.inc');

class knjl342kController extends Controller {
    var $ModelClassName = "knjl342kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl342k":
                    $sessionInstance->knjl342kModel();
                    $this->callView("knjl342kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl342kCtl = new knjl342kController;
var_dump($_REQUEST);
?>
