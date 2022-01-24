<?php

require_once('for_php7.php');

require_once('knje070dModel.inc');
require_once('knje070dQuery.inc');

class knje070dController extends Controller
{
    public $ModelClassName = "knje070dModel";
    public $ProgramID      = "KNJE070D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje070d":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knje070dModel();       //コントロールマスタの呼び出し
                    $this->callView("knje070dForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje070dForm1");
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
$knje070dCtl = new knje070dController();
//var_dump($_REQUEST);
?>
