<?php

require_once('for_php7.php');
require_once('knjd627iModel.inc');
require_once('knjd627iQuery.inc');

class knjd627iController extends Controller
{
    public $ModelClassName = "knjd627iModel";
    public $ProgramID      = "KNJD627I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        $sessionInstance->programID = $this->ProgramID;

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "change":
                    $this->callView("knjd627iForm1");
                    break 2;
                case "":
                case "main":
                    $this->callView("knjd627iForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd627iCtl = new knjd627iController();
