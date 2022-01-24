<?php

require_once('for_php7.php');

require_once('knjd626aModel.inc');
require_once('knjd626aQuery.inc');

class knjd626aController extends Controller
{
    public $ModelClassName = "knjd626aModel";
    public $ProgramID      = "KNJD626A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "clear":
                case "knjd626a":
                    $sessionInstance->knjd626aModel();
                    $this->callView("knjd626aForm1");
                    exit;
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjd626aModel();
                    $this->callView("knjd626aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd626aCtl = new knjd626aController();
