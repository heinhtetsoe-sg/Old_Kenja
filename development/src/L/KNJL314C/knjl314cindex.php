<?php

require_once('for_php7.php');

require_once('knjl314cModel.inc');
require_once('knjl314cQuery.inc');

class knjl314cController extends Controller {
    var $ModelClassName = "knjl314cModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl314c":
                    $sessionInstance->knjl314cModel();
                    $this->callView("knjl314cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl314cCtl = new knjl314cController;
//var_dump($_REQUEST);
?>
