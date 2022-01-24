<?php

require_once('for_php7.php');

require_once('knjl380jModel.inc');
require_once('knjl380jQuery.inc');

class knjl380jController extends Controller {
    var $ModelClassName = "knjl380jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl380j":
                    $sessionInstance->knjl380jModel();
                    $this->callView("knjl380jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl380jCtl = new knjl380jController;
var_dump($_REQUEST);
?>
