<?php

require_once('for_php7.php');

require_once('knje373gModel.inc');
require_once('knje373gQuery.inc');

class knje373gController extends Controller
{
    public $ModelClassName = "knje373gModel";
    public $ProgramID      = "KNJE373G";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje373g":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje373gModel();       //コントロールマスタの呼び出し
                    $this->callView("knje373gForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje373gForm1");
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
$knje373gCtl = new knje373gController();
