<?php

require_once('knjl373iModel.inc');
require_once('knjl373iQuery.inc');

class knjl373iController extends Controller
{
    public $ModelClassName = "knjl373iModel";
    public $ProgramID      = "KNJL373I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl373iForm1");
                    break 2;
                case "knjl373i":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl373iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjl373iForm1");
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
$knjl373iCtl = new knjl373iController();
