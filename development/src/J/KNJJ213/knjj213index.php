<?php

require_once('for_php7.php');

require_once('knjj213Model.inc');
require_once('knjj213Query.inc');

class knjj213Controller extends Controller {
    var $ModelClassName = "knjj213Model";
    var $ProgramID      = "KNJJ213";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "hukusiki":
                case "change_class":
                case "knjj213":
                    $sessionInstance->knjj213Model();
                    $this->callView("knjj213Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj213Ctl = new knjj213Controller;
var_dump($_REQUEST);
?>
