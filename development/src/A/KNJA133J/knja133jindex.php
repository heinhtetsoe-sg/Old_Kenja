<?php

require_once('for_php7.php');

require_once('knja133jModel.inc');
require_once('knja133jQuery.inc');

class knja133jController extends Controller
{
    public $ModelClassName = "knja133jModel";
    public $ProgramID      = "KNJA133J";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja133j":                            //メニュー画面もしくはSUBMITした場合
                case "print":
                    $sessionInstance->knja133jModel();      //コントロールマスタの呼び出し
                    $this->callView("knja133jForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja133jModel();      //コントロールマスタの呼び出し
                    $this->callView("knja133jForm1");
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
$knja133jCtl = new knja133jController();
