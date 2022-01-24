<?php
require_once('knjl362kModel.inc');
require_once('knjl362kQuery.inc');

class knjl362kController extends Controller {
    var $ModelClassName = "knjl362kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl362k":
                    $sessionInstance->knjl362kModel();
                    $this->callView("knjl362kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl362kCtl = new knjl362kController;
var_dump($_REQUEST);
?>
