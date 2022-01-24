<?php

require_once('for_php7.php');

require_once('knje372jModel.inc');
require_once('knje372jQuery.inc');

class knje372jController extends Controller
{
    public $ModelClassName = "knje372jModel";
    public $ProgramID      = "KNJE372J";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje372j":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                case "change_semes":
                    $sessionInstance->knje372jModel();        //コントロールマスタの呼び出し
                    $this->callView("knje372jForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje372jForm1");
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
$knje372jCtl = new knje372jController();
