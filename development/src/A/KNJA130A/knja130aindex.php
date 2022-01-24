<?php

require_once('for_php7.php');

require_once('knja130aModel.inc');
require_once('knja130aQuery.inc');

class knja130aController extends Controller
{
    public $ModelClassName = "knja130aModel";
    public $ProgramID      = "KNJA130A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja130a":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja130aModel();      //コントロールマスタの呼び出し
                    $this->callView("knja130aForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合 //NO002
                    $sessionInstance->knja130aModel();      //コントロールマスタの呼び出し
                    $this->callView("knja130aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knja130aForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja130aCtl = new knja130aController();
//var_dump($_REQUEST);
