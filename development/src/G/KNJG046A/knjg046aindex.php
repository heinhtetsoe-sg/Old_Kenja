<?php

require_once('for_php7.php');

require_once('knjg046aModel.inc');
require_once('knjg046aQuery.inc');

class knjg046aController extends Controller
{
    var $ModelClassName = "knjg046aModel";
    var $ProgramID      = "KNJG046A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjg046aModel();
                    $this->callView("knjg046aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg046aCtl = new knjg046aController();
?>
