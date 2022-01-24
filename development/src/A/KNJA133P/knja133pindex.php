<?php

require_once('for_php7.php');

require_once('knja133pModel.inc');
require_once('knja133pQuery.inc');

class knja133pController extends Controller
{
    public $ModelClassName = "knja133pModel";
    public $ProgramID      = "KNJA133P";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja133p":                            //メニュー画面もしくはSUBMITした場合
                case "print":
                    $sessionInstance->knja133pModel();      //コントロールマスタの呼び出し
                    $this->callView("knja133pForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja133pModel();      //コントロールマスタの呼び出し
                    $this->callView("knja133pForm1");
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
$knja133pCtl = new knja133pController();
