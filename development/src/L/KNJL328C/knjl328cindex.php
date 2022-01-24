<?php

require_once('for_php7.php');

require_once('knjl328cModel.inc');
require_once('knjl328cQuery.inc');

class knjl328cController extends Controller {
    var $ModelClassName = "knjl328cModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl328c":
                    $sessionInstance->knjl328cModel();
                    $this->callView("knjl328cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl328cCtl = new knjl328cController;
//var_dump($_REQUEST);
?>
