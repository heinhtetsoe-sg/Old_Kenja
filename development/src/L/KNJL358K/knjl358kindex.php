<?php
require_once('knjl358kModel.inc');
require_once('knjl358kQuery.inc');

class knjl358kController extends Controller {
    var $ModelClassName = "knjl358kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl358k":
                    $sessionInstance->knjl358kModel();
                    $this->callView("knjl358kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl358kCtl = new knjl358kController;
var_dump($_REQUEST);
?>
