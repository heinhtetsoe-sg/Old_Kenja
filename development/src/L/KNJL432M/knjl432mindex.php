<?php

require_once('for_php7.php');

require_once('knjl432mModel.inc');
require_once('knjl432mQuery.inc');

class knjl432mController extends Controller
{
    public $ModelClassName = "knjl432mModel";
    public $ProgramID      = "KNJL432M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "search":
                case "reset":
                case "end":
                case "back":
                    $this->callView("knjl432mForm1");
                    break 2;
                case "allpass":
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                case "form2":
                    $this->callView("knjl432mForm2");
                    break 2;
                case "exec":    //CSV取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("form2");
                    break 1;
                case "csv":     //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $sessionInstance->setCmd("form2");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl432mCtl = new knjl432mController();
