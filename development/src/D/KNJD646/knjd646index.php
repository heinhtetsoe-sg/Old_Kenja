<?php

require_once('for_php7.php');

require_once('knjd646Model.inc');
require_once('knjd646Query.inc');

class knjd646Controller extends Controller {
    var $ModelClassName = "knjd646Model";
    var $ProgramID      = "KNJD646";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd646":
                    $sessionInstance->knjd646Model();
                    $this->callView("knjd646Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd646Ctl = new knjd646Controller;
var_dump($_REQUEST);
?>
