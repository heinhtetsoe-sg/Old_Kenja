<?php

require_once('for_php7.php');

require_once('knjd157aModel.inc');
require_once('knjd157aQuery.inc');

class knjd157aController extends Controller
{
    public $ModelClassName = "knjd157aModel";
    public $ProgramID      = "KNJD157A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd157a":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                    //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                case "change_semes":
                    $sessionInstance->knjd157aModel();          //コントロールマスタの呼び出し
                    $this->callView("knjd157aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd157aForm1");
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
$knjd157aCtl = new knjd157aController();
