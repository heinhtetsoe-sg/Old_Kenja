<?php

require_once('for_php7.php');

require_once('knjp740aModel.inc');
require_once('knjp740aQuery.inc');

class knjp740aController extends Controller
{
    public $ModelClassName = "knjp740aModel";
    public $ProgramID      = "KNJP740A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "csvPop":
                case "knjp740a":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp740aModel();       //コントロールマスタの呼び出し
                    $this->callView("knjp740aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    $sessionInstance->getDownloadModel();
                    $sessionInstance->setCmd("csvPop");
                    break 1;
                case "csv2":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjp740aForm1");
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
$knjp740aCtl = new knjp740aController();
