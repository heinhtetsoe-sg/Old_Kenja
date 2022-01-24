<?php

require_once('for_php7.php');

require_once('knjm804Model.inc');
require_once('knjm804Query.inc');

class knjm804Controller extends Controller {
    var $ModelClassName = "knjm804Model";
    var $ProgramID      = "KNJM804";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_class":
                case "knjm804":
                    $sessionInstance->knjm804Model();
                    $this->callView("knjm804Form1");
                    exit;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm804Ctl = new knjm804Controller;
var_dump($_REQUEST);
?>
