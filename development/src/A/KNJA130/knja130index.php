<?php

require_once('for_php7.php');

require_once('knja130Model.inc');
require_once('knja130Query.inc');

class knja130Controller extends Controller
{
    public $ModelClassName = "knja130Model";
    public $ProgramID      = "KNJA130";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja130":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja130Model();       //コントロールマスタの呼び出し
                    $this->callView("knja130Form1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合 //NO002
                    $sessionInstance->knja130Model();       //コントロールマスタの呼び出し
                    $this->callView("knja130Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knja130Form1");
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
$knja130Ctl = new knja130Controller();
//var_dump($_REQUEST);
