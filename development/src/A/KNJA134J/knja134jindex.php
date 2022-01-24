<?php

require_once('for_php7.php');

require_once('knja134jModel.inc');
require_once('knja134jQuery.inc');

class knja134jController extends Controller
{
    public $ModelClassName = "knja134jModel";
    public $ProgramID      = "KNJA134J";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja134j":                            //メニュー画面もしくはSUBMITした場合
                case "print":
                    $sessionInstance->knja134jModel();      //コントロールマスタの呼び出し
                    $this->callView("knja134jForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja134jModel();      //コントロールマスタの呼び出し
                    $this->callView("knja134jForm1");
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
$knja134jCtl = new knja134jController();
