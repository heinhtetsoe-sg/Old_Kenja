<?php

require_once('for_php7.php');

require_once('knjl311Model.inc');
require_once('knjl311Query.inc');

class knjl311Controller extends Controller {
    var $ModelClassName = "knjl311Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl311":
                    $sessionInstance->knjl311Model();
                    $this->callView("knjl311Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl311Ctl = new knjl311Controller;
var_dump($_REQUEST);
?>
