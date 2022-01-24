<?php
require_once('knje371aModel.inc');
require_once('knje371aQuery.inc');

class knje371aController extends Controller
{
    public $ModelClassName = "knje371aModel";
    public $ProgramID      = "KNJE371A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje371a":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje371aModel();       //コントロールマスタの呼び出し
                    $this->callView("knje371aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knje371aForm1");
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
$knje371aCtl = new knje371aController;
