<?php

require_once('for_php7.php');

require_once('knjh400_TyousasyoSyuusyokuModel.inc');
require_once('knjh400_TyousasyoSyuusyokuQuery.inc');

class knjh400_TyousasyoSyuusyokuController extends Controller
{
    public $ModelClassName = "knjh400_TyousasyoSyuusyokuModel";
    public $ProgramID      = "KNJH400";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "updEdit":
                case "edit":
                case "tsuchiTorikomi":
                case "select_pattern":
                    $this->callView("knjh400_TyousasyoSyuusyokuForm1");
                    break 2;
                case "subform1": //成績参照
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_TyousasyoSyuusyokuSubForm1");
                    break 2;
                case "list":
                    break 2;
                case "detail":
                    break 2;
                case "reset":
                    $this->callView("knjh400_TyousasyoSyuusyokuForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjh400_TyousasyoSyuusyokuForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh400_TyousasyoSyuusyokuCtl = new knjh400_TyousasyoSyuusyokuController();
//var_dump($_REQUEST);
