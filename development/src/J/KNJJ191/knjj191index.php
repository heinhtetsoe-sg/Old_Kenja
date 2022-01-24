<?php

require_once('for_php7.php');

require_once('knjj191Model.inc');
require_once('knjj191Query.inc');

class knjj191Controller extends Controller {
    var $ModelClassName = "knjj191Model";
    var $ProgramID      = "KNJJ191";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjj191":
                    $sessionInstance->knjj191Model();
                    $this->callView("knjj191Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj191Ctl = new knjj191Controller;
//var_dump($_REQUEST);
?>
