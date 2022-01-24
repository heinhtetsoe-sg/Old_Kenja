<?php

require_once('for_php7.php');

require_once('knjd157bModel.inc');
require_once('knjd157bQuery.inc');

class knjd157bController extends Controller
{
    public $ModelClassName = "knjd157bModel";
    public $ProgramID      = "KNJD157B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd157b":                            //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                case "change_semes":
                    $sessionInstance->knjd157bModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd157bForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd157bForm1");
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
$knjd157bCtl = new knjd157bController();
