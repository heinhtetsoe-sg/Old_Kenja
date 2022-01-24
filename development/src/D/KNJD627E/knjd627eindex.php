<?php

require_once('for_php7.php');
require_once('knjd627eModel.inc');
require_once('knjd627eQuery.inc');

class knjd627eController extends Controller
{
    public $ModelClassName = "knjd627eModel";
    public $ProgramID      = "KNJD627E";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        $sessionInstance->programID = $this->ProgramID;

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "change":
                    $this->callView("knjd627eForm1");
                    break 2;
                case "":
                case "main":
                    $this->callView("knjd627eForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd627eCtl = new knjd627eController();
