<?php

require_once('for_php7.php');

require_once('knja142qModel.inc');
require_once('knja142qQuery.inc');

class knja142qController extends Controller
{
    public $ModelClassName = "knja142qModel";
    public $ProgramID      = "KNJA142Q";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja142q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja142qModel();      //コントロールマスタの呼び出し
                    $this->callView("knja142qForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knja142qForm1");
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
$knja142qCtl = new knja142qController;
