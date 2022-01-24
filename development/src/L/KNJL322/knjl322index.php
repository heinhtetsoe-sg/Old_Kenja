<?php

require_once('for_php7.php');

require_once('knjl322Model.inc');
require_once('knjl322Query.inc');

class knjl322Controller extends Controller {
    var $ModelClassName = "knjl322Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl322":
                    $sessionInstance->knjl322Model();
                    $this->callView("knjl322Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl322Ctl = new knjl322Controller;
var_dump($_REQUEST);
?>
