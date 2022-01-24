<?php

require_once('for_php7.php');

require_once('knje372iModel.inc');
require_once('knje372iQuery.inc');

class knje372iController extends Controller
{
    public $ModelClassName = "knje372iModel";
    public $ProgramID      = "KNJE372I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knje372iModel();        //コントロールマスタの呼び出し
                    $this->callView("knje372iForm1");
                    exit;
                case "knje372i":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knje372iModel();        //コントロールマスタの呼び出し
                    $this->callView("knje372iForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje372iForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje372iCtl = new knje372iController;
