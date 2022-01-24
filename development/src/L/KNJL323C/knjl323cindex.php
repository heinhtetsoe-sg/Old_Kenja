<?php

require_once('for_php7.php');

require_once('knjl323cModel.inc');
require_once('knjl323cQuery.inc');

class knjl323cController extends Controller {
    var $ModelClassName = "knjl323cModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl323c":
                    $sessionInstance->knjl323cModel();
                    $this->callView("knjl323cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl323cCtl = new knjl323cController;
//var_dump($_REQUEST);
?>
