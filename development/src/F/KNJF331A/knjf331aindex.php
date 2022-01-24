<?php

require_once('for_php7.php');

require_once('knjf331aModel.inc');
require_once('knjf331aQuery.inc');

class knjf331aController extends Controller {
    var $ModelClassName = "knjf331aModel";
    var $ProgramID      = "KNJF331A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf331a":
                    $sessionInstance->knjf331aModel();
                    $this->callView("knjf331aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf331aCtl = new knjf331aController;
?>
