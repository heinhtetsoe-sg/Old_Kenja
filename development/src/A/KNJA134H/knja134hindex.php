<?php

require_once('for_php7.php');

require_once('knja134hModel.inc');
require_once('knja134hQuery.inc');

class knja134hController extends Controller
{
    public $ModelClassName = "knja134hModel";
    public $ProgramID      = "KNJA134H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja134h":                             //メニュー画面もしくはSUBMITした場合
                case "print":
                    $sessionInstance->knja134hModel();       //コントロールマスタの呼び出し
                    $this->callView("knja134hForm1");
                    exit;
                case "chitekichange":                         //メニュー画面もしくはSUBMITした場合 //NO002
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合 //NO002
                    $sessionInstance->knja134hModel();       //コントロールマスタの呼び出し
                    $this->callView("knja134hForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knja134hForm1");
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
$knja134hCtl = new knja134hController();
//var_dump($_REQUEST);
