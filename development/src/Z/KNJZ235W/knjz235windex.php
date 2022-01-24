<?php

require_once('for_php7.php');
require_once('knjz235wModel.inc');
require_once('knjz235wQuery.inc');

class knjz235wController extends Controller
{
    public $ModelClassName = "knjz235wModel";
    public $ProgramID      = "KNJZ235W";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "reset":
                case "changeKind":
                case "edit":
                    if ($sessionInstance->pattern == 'D429LM') {
                        $this->callView("knjz235wForm8");
                    } elseif ($sessionInstance->pattern == 'D429BD') {
                        $this->callView("knjz235wForm7");
                    } elseif ($sessionInstance->pattern == 'D429') {
                        $this->callView("knjz235wForm6");
                    } elseif ($sessionInstance->pattern == 'D185E') {
                        $this->callView("knjz235wForm5");
                    } elseif ($sessionInstance->pattern == 'D181H' || $sessionInstance->pattern == 'D181A') {
                        $this->callView("knjz235wForm4");
                    } elseif ($sessionInstance->pattern == 'D186V') {
                        $this->callView("knjz235wForm3");
                    } elseif ($sessionInstance->pattern == 'D185W') {
                        $this->callView("knjz235wForm2");
                    } else {
                        $this->callView("knjz235wForm1");
                    }
                    break 2;
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":    //前年度コピー
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("edit");
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
$knjz235wCtl = new knjz235wController();
