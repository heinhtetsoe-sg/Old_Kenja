<?php

require_once('for_php7.php');

require_once('knjl315Model.inc');
require_once('knjl315Query.inc');

class knjl315Controller extends Controller {
    var $ModelClassName = "knjl315Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl315":
                    $sessionInstance->knjl315Model();
                    $this->callView("knjl315Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl315Ctl = new knjl315Controller;
var_dump($_REQUEST);
?>
