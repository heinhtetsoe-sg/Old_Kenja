<?php

require_once('knjl379iModel.inc');
require_once('knjl379iQuery.inc');

class knjl379iController extends Controller
{
    public $ModelClassName = "knjl379iModel";
    public $ProgramID      = "KNJL379I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl379iForm1");
                    break 2;
                case "knjl379i":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl379iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjl379iForm1");
                    exit;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
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
$knjl379iCtl = new knjl379iController;
