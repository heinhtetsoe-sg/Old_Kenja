<?php

require_once('knjl378iModel.inc');
require_once('knjl378iQuery.inc');

class knjl378iController extends Controller
{
    public $ModelClassName = "knjl378iModel";
    public $ProgramID      = "KNJL378I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl378iForm1");
                    break 2;
                case "knjl378i":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl378iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjl378iForm1");
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
$knjl378iCtl = new knjl378iController;
