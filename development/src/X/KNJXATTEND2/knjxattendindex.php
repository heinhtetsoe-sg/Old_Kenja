<?php

require_once('for_php7.php');

require_once('knjxattendModel.inc');
require_once('knjxattendQuery.inc');

class knjxattendController extends Controller {
    var $ModelClassName = "knjxattendModel";
    var $ProgramID      = "knjxattend";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "yearChange":
                    $this->callView("knjxattendForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxattendCtl = new knjxattendController;
?>
