<?php

require_once('for_php7.php');

require_once('knjh400_kekkazisuuModel.inc');
require_once('knjh400_kekkazisuuQuery.inc');

class knjh400_kekkazisuuController extends Controller
{
    public $ModelClassName = "knjh400_kekkazisuuModel";
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
                    $this->callView("knjh400_kekkazisuuForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh400_kekkazisuuCtl = new knjh400_kekkazisuuController();
