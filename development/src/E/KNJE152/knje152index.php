<?php

require_once('for_php7.php');

require_once('knje152Model.inc');
require_once('knje152Query.inc');

class knje152Controller extends Controller
{
    public $ModelClassName = "knje152Model";
    public $ProgramID      = "KNJE152";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje152":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje152Model();       //コントロールマスタの呼び出し
                    $this->callView("knje152Form1");
                    exit;
                case "csv1":     //CSVダウンロード
                case "csv2":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje152Form1");
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
$knje152Ctl = new knje152Controller();
