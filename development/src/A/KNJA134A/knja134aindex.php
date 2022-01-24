<?php

require_once('for_php7.php');

require_once('knja134aModel.inc');
require_once('knja134aQuery.inc');

class knja134aController extends Controller
{
    public $ModelClassName = "knja134aModel";
    public $ProgramID      = "KNJA134A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja134a":                             //メニュー画面もしくはSUBMITした場合
                case "print":
                    $sessionInstance->knja134aModel();       //コントロールマスタの呼び出し
                    $this->callView("knja134aForm1");
                    exit;
                case "chitekichange":                         //メニュー画面もしくはSUBMITした場合 //NO002
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合 //NO002
                    $sessionInstance->knja134aModel();       //コントロールマスタの呼び出し
                    $this->callView("knja134aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knja134aForm1");
                    }
                    break 2;
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
$knja134aCtl = new knja134aController();
//var_dump($_REQUEST);
