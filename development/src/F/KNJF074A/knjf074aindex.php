<?php

require_once('for_php7.php');

require_once('knjf074aModel.inc');
require_once('knjf074aQuery.inc');

class knjf074aController extends Controller
{
    public $ModelClassName = "knjf074aModel";
    public $ProgramID      = "KNJF074A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf074a":                        //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf074aModel();  //コントロールマスタの呼び出し
                    $this->callView("knjf074aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjf074aForm1");
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
$knjf074aCtl = new knjf074aController();
