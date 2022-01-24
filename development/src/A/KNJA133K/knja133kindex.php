<?php

require_once('for_php7.php');

require_once('knja133kModel.inc');
require_once('knja133kQuery.inc');

class knja133kController extends Controller
{
    public $ModelClassName = "knja133kModel";
    public $ProgramID      = "KNJA133K";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja133k":                            //メニュー画面もしくはSUBMITした場合
                case "print":
                    $sessionInstance->knja133kModel();      //コントロールマスタの呼び出し
                    $this->callView("knja133kForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja133kModel();      //コントロールマスタの呼び出し
                    $this->callView("knja133kForm1");
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
$knja133kCtl = new knja133kController();
