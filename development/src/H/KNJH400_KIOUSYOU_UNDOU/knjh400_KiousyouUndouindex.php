<?php

require_once('for_php7.php');
require_once('knjh400_KiousyouUndouModel.inc');
require_once('knjh400_KiousyouUndouQuery.inc');

class knjh400_KiousyouUndouController extends Controller
{
    public $ModelClassName = "knjh400_KiousyouUndouModel";
    public $ProgramID      = "KNJH400";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_KiousyouUndouForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh400_KiousyouUndouCtl = new knjh400_KiousyouUndouController();
