<?php

require_once('for_php7.php');

require_once('knjf323aModel.inc');
require_once('knjf323aQuery.inc');

class knjf323aController extends Controller
{
    public $ModelClassName = "knjf323aModel";
    public $ProgramID      = "KNJF323A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf323a":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjf323aModel();      //コントロールマスタの呼び出し
                    $this->callView("knjf323aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $this->ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjf323aForm1");
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
$knjf323aCtl = new knjf323aController();
