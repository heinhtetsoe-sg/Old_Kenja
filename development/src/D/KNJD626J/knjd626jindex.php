<?php

require_once('for_php7.php');

require_once('knjd626jModel.inc');
require_once('knjd626jQuery.inc');

class knjd626jController extends Controller
{
    public $ModelClassName = "knjd626jModel";
    public $ProgramID      = "KNJD626J";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd626j":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd626jModel();   //コントロールマスタの呼び出し
                    $this->callView("knjd626jForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd626jForm1");
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
$knjd626jCtl = new knjd626jController();
//var_dump($_REQUEST);
