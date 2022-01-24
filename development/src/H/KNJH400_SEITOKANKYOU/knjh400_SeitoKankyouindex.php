<?php

require_once('for_php7.php');

require_once('knjh400_SeitoKankyouModel.inc');
require_once('knjh400_SeitoKankyouQuery.inc');

class knjh400_SeitoKankyouController extends Controller
{
    public $ModelClassName = "knjh400_SeitoKankyouModel";
    public $ProgramID      = "KNJH400";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_SeitoKankyouForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "back":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_SeitoKankyouForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh400_SeitoKankyouCtl = new knjh400_SeitoKankyouController();
