<?php

require_once('for_php7.php');

require_once('knjl355cModel.inc');
require_once('knjl355cQuery.inc');

class knjl355cController extends Controller {
    var $ModelClassName = "knjl355cModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl355c":
                    $sessionInstance->knjl355cModel();
                    $this->callView("knjl355cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl355cCtl = new knjl355cController;
//var_dump($_REQUEST);
?>
