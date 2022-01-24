<?php

require_once('for_php7.php');
require_once('knjd187rModel.inc');
require_once('knjd187rQuery.inc');

class knjd187rController extends Controller
{
    public $ModelClassName = "knjd187rModel";
    public $ProgramID      = "KNJD187R";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        $sessionInstance->programID = $this->ProgramID;

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "change":
                    $this->callView("knjd187rForm1");
                    break 2;
                case "":
                case "main":
                    $this->callView("knjd187rForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd187rCtl = new knjd187rController();
