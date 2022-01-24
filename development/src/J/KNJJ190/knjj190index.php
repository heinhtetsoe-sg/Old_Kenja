<?php

require_once('for_php7.php');

require_once('knjj190Model.inc');
require_once('knjj190Query.inc');

class knjj190Controller extends Controller {
    var $ModelClassName = "knjj190Model";
    var $ProgramID      = "KNJJ190";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjj190":
                    $sessionInstance->knjj190Model();
                    $this->callView("knjj190Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj190Ctl = new knjj190Controller;
//var_dump($_REQUEST);
?>
