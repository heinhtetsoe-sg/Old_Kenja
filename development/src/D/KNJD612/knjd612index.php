<?php

require_once('for_php7.php');

require_once('knjd612Model.inc');
require_once('knjd612Query.inc');

class knjd612Controller extends Controller {
    var $ModelClassName = "knjd612Model";
    var $ProgramID      = "KNJD612";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd612":
                case "gakki":
                    $sessionInstance->knjd612Model();
                    $this->callView("knjd612Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd612Ctl = new knjd612Controller;
var_dump($_REQUEST);
?>
