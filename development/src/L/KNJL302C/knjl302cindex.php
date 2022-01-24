<?php

require_once('for_php7.php');

require_once('knjl302cModel.inc');
require_once('knjl302cQuery.inc');

class knjl302cController extends Controller {
    var $ModelClassName = "knjl302cModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl302c":
                    $sessionInstance->knjl302cModel();
                    $this->callView("knjl302cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl302cCtl = new knjl302cController;
//var_dump($_REQUEST);
?>
