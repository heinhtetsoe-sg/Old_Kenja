<?php

require_once('for_php7.php');
require_once('knjd627bModel.inc');
require_once('knjd627bQuery.inc');

class knjd627bController extends Controller
{
    public $ModelClassName = "knjd627bModel";
    public $ProgramID      = "KNJD627B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        $sessionInstance->programID = $this->ProgramID;

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "change":
                    $this->callView("knjd627bForm1");
                    break 2;
                case "":
                case "main":
                    $this->callView("knjd627bForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd627bCtl = new knjd627bController();
