<?php

require_once('for_php7.php');

require_once('knjl328aModel.inc');
require_once('knjl328aQuery.inc');

class knjl328aController extends Controller
{
    public $ModelClassName = "knjl328aModel";
    public $ProgramID      = "KNJL328A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl328a":
                case "print":
                case "chgApplicantdiv":
                case "chgPatternCd":
                case "chgTokutaiSelect":
                    $this->callView("knjl328aForm1");
                    break 2;
                case "updateAndPrint":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("print");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl328aCtl = new knjl328aController();
