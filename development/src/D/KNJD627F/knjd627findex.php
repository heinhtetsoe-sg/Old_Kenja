<?php

require_once('for_php7.php');
require_once('knjd627fModel.inc');
require_once('knjd627fQuery.inc');

class knjd627fController extends Controller
{
    public $ModelClassName = "knjd627fModel";
    public $ProgramID      = "KNJD627F";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        $sessionInstance->programID = $this->ProgramID;

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csvoutput":
                    if (!$sessionInstance->outputCSV()) {
                        $this->callView("knjd627fForm1");
                    }
                    break 2;
                case "":
                case "change":
                case "main":
                    $this->callView("knjd627fForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd627fCtl = new knjd627fController();
