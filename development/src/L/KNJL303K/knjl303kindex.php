<?php
require_once('knjl303kModel.inc');
require_once('knjl303kQuery.inc');

class knjl303kController extends Controller {
    var $ModelClassName = "knjl303kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl303k":
                    $sessionInstance->knjl303kModel();
                    $this->callView("knjl303kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl303kCtl = new knjl303kController;
var_dump($_REQUEST);
?>
