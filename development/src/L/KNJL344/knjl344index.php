<?php

require_once('for_php7.php');

require_once('knjl344Model.inc');
require_once('knjl344Query.inc');

class knjl344Controller extends Controller {
    var $ModelClassName = "knjl344Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl344":
                    $sessionInstance->knjl344Model();
                    $this->callView("knjl344Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl344Ctl = new knjl344Controller;
var_dump($_REQUEST);
?>
