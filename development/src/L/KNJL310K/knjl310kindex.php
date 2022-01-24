<?php
require_once('knjl310kModel.inc');
require_once('knjl310kQuery.inc');

class knjl310kController extends Controller {
    var $ModelClassName = "knjl310kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl310k":
                    $sessionInstance->knjl310kModel();
                    $this->callView("knjl310kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl310kCtl = new knjl310kController;
var_dump($_REQUEST);
?>
