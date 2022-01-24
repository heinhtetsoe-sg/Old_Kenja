<?php

require_once('for_php7.php');
require_once('knjl371iModel.inc');
require_once('knjl371iQuery.inc');

class knjl371iController extends Controller
{
    public $ModelClassName = "knjl371iModel";
    public $ProgramID      = "KNJL371I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl371iForm1");
                    break 2;
                case "knjl371i":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl371iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjl371iForm1");
                    exit;
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
$knjl371iCtl = new knjl371iController();
