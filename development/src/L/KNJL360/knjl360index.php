<?php

require_once('for_php7.php');

require_once('knjl360Model.inc');
require_once('knjl360Query.inc');

class knjl360Controller extends Controller {
    var $ModelClassName = "knjl360Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl360":
                    $sessionInstance->knjl360Model();
                    $this->callView("knjl360Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl360Ctl = new knjl360Controller;
var_dump($_REQUEST);
?>
