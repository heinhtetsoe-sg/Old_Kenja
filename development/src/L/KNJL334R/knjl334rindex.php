<?php

require_once('for_php7.php');

require_once('knjl334rModel.inc');
require_once('knjl334rQuery.inc');

class knjl334rController extends Controller {
    var $ModelClassName = "knjl334rModel";
    var $ProgramID      = "KNJL334R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl334r":
                    $sessionInstance->knjl334rModel();
                    $this->callView("knjl334rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl334rCtl = new knjl334rController;
//var_dump($_REQUEST);
?>
