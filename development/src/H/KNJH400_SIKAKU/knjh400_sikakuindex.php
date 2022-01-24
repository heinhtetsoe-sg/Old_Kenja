<?php

require_once('for_php7.php');

require_once('knjh400_sikakuModel.inc');
require_once('knjh400_sikakuQuery.inc');

class knjh400_sikakuController extends Controller
{
    public $ModelClassName = "knjh400_sikakuModel";
    public $ProgramID      = "KNJH400";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "error":
                    $this->callView("error");
                    break 2;
                case "edit":
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_sikakuForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh400_sikakuCtl = new knjh400_sikakuController();
