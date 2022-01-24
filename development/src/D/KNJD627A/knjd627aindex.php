<?php

require_once('for_php7.php');

require_once('knjd627aModel.inc');
require_once('knjd627aQuery.inc');

class knjd627aController extends Controller
{
    public $ModelClassName = "knjd627aModel";
    public $ProgramID      = "KNJD627A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $this->callView("knjd627aForm1");
                    break 2;
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
$knjd627aCtl = new knjd627aController();
