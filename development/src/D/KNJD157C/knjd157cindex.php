<?php

require_once('for_php7.php');

require_once('knjd157cModel.inc');
require_once('knjd157cQuery.inc');

class knjd157cController extends Controller
{
    public $ModelClassName = "knjd157cModel";
    public $ProgramID      = "KNJD157C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd157c":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                    //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                case "change_semes":
                    $sessionInstance->knjd157cModel();          //コントロールマスタの呼び出し
                    $this->callView("knjd157cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd157cForm1");
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
$knjd157cCtl = new knjd157cController();
