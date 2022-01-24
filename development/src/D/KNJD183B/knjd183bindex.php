<?php

require_once('for_php7.php');
require_once('knjd183bModel.inc');
require_once('knjd183bQuery.inc');

class knjd183bController extends Controller
{
    public $ModelClassName = "knjd183bModel";
    public $ProgramID      = "KNJD183B";

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
                        $this->callView("knjd183bForm8");
                    } elseif ($sessionInstance->pattern == 'D429BD') {
                        $this->callView("knjd183bForm7");
                    } elseif ($sessionInstance->pattern == 'D429') {
                        $this->callView("knjd183bForm6");
                    } elseif ($sessionInstance->pattern == 'D185E') {
                        $this->callView("knjd183bForm5");
                    } elseif ($sessionInstance->pattern == 'D181H' || $sessionInstance->pattern == 'D181A') {
                        $this->callView("knjd183bForm4");
                    } elseif ($sessionInstance->pattern == 'D186V') {
                        $this->callView("knjd183bForm3");
                    } elseif ($sessionInstance->pattern == 'D185W') {
                        $this->callView("knjd183bForm2");
                    } else {
                        $this->callView("knjd183bForm1");
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
$knjd183bCtl = new knjd183bController();
