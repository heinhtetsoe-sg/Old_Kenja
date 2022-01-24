<?php

require_once('for_php7.php');
require_once('knjd301bModel.inc');
require_once('knjd301bQuery.inc');

class knjd301bController extends Controller
{
    public $ModelClassName = "knjd301bModel";
    public $ProgramID      = "KNJD301B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        $sessionInstance->programID = $this->ProgramID;

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "change":
                    $this->callView("knjd301bForm1");
                    break 2;
                case "csvoutput":
                    if (!$sessionInstance->outputCSV()) {
                        $this->callView("knjd301bForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjd301bForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd301bCtl = new knjd301bController();
