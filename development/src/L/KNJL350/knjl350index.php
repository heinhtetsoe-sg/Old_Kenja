<?php

require_once('for_php7.php');

require_once('knjl350Model.inc');
require_once('knjl350Query.inc');

class knjl350Controller extends Controller {
    var $ModelClassName = "knjl350Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl350":
                    $sessionInstance->knjl350Model();
                    $this->callView("knjl350Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl350Ctl = new knjl350Controller;
var_dump($_REQUEST);
?>
