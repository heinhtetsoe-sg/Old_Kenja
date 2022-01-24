<?php

require_once('for_php7.php');

require_once('knja130dModel.inc');
require_once('knja130dQuery.inc');

class knja130dController extends Controller
{
    public $ModelClassName = "knja130dModel";
    public $ProgramID      = "KNJA130D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja130d":                             //メニュー画面もしくはSUBMITした場合
                case "print":
                    $sessionInstance->knja130dModel();       //コントロールマスタの呼び出し
                    $this->callView("knja130dForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合 //NO002
                    $sessionInstance->knja130dModel();       //コントロールマスタの呼び出し
                    $this->callView("knja130dForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knja130dForm1");
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
$knja130dCtl = new knja130dController();
//var_dump($_REQUEST);
