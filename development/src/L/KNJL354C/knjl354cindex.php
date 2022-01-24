<?php

require_once('for_php7.php');

require_once('knjl354cModel.inc');
require_once('knjl354cQuery.inc');

class knjl354cController extends Controller {
    var $ModelClassName = "knjl354cModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl354c":
                    $sessionInstance->knjl354cModel();
                    $this->callView("knjl354cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl354cCtl = new knjl354cController;
//var_dump($_REQUEST);
?>
