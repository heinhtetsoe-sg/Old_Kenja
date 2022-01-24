<?php

require_once('for_php7.php');

require_once('knja228aModel.inc');
require_once('knja228aQuery.inc');

class knja228aController extends Controller
{
    public $ModelClassName = "knja228aModel";
    public $ProgramID        = "KNJA228A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja228a":                             //メニュー画面もしくはSUBMITした場合
                case "change_class":                        //クラス変更時のSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knja228aModel();       //コントロールマスタの呼び出し
                    $this->callView("knja228aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knja228aForm1");
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
$knja228aCtl = new knja228aController();
