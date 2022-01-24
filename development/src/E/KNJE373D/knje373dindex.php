<?php

require_once('for_php7.php');

require_once('knje373dModel.inc');
require_once('knje373dQuery.inc');

class knje373dController extends Controller
{
    public $ModelClassName = "knje373dModel";
    public $ProgramID      = "KNJE373D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje373d":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje373dModel();       //コントロールマスタの呼び出し
                    $this->callView("knje373dForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje373dForm1");
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
$knje373dCtl = new knje373dController();
