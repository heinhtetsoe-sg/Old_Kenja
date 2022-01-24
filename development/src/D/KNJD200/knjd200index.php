<?php

require_once('for_php7.php');

require_once('knjd200Model.inc');
require_once('knjd200Query.inc');

class knjd200Controller extends Controller {
    var $ModelClassName = "knjd200Model";
    var $ProgramID      = "KNJD200";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd200":
                    $sessionInstance->knjd200Model();
                    $this->callView("knjd200Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd200Ctl = new knjd200Controller;
var_dump($_REQUEST);
?>
