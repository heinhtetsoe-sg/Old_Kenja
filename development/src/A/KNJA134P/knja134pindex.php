<?php

require_once('for_php7.php');

require_once('knja134pModel.inc');
require_once('knja134pQuery.inc');

class knja134pController extends Controller
{
    public $ModelClassName = "knja134pModel";
    public $ProgramID      = "KNJA134P";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja134p":                            //メニュー画面もしくはSUBMITした場合
                case "print":
                    $sessionInstance->knja134pModel();      //コントロールマスタの呼び出し
                    $this->callView("knja134pForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja134pModel();      //コントロールマスタの呼び出し
                    $this->callView("knja134pForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("print");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja134pCtl = new knja134pController();
