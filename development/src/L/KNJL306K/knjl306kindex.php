<?php
require_once('knjl306kModel.inc');
require_once('knjl306kQuery.inc');

class knjl306kController extends Controller {
    var $ModelClassName = "knjl306kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl306k":
                    $sessionInstance->knjl306kModel();
                    $this->callView("knjl306kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl306kCtl = new knjl306kController;
var_dump($_REQUEST);
?>
