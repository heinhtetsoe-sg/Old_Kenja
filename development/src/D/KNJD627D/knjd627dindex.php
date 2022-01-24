<?php

require_once('for_php7.php');
require_once('knjd627dModel.inc');
require_once('knjd627dQuery.inc');

class knjd627dController extends Controller
{
    public $ModelClassName = "knjd627dModel";
    public $ProgramID      = "KNJD627D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        $sessionInstance->programID = $this->ProgramID;

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "change":
                    $this->callView("knjd627dForm1");
                    break 2;
                case "":
                case "main":
                    $this->callView("knjd627dForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd627dCtl = new knjd627dController();
