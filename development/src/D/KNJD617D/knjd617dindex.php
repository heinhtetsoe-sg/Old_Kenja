<?php

require_once('for_php7.php');

require_once('knjd617dModel.inc');
require_once('knjd617dQuery.inc');

class knjd617dController extends Controller
{
    public $ModelClassName = "knjd617dModel";
    public $ProgramID      = "KNJD617D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd617dModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd617dForm1");
                    exit;
                case "knjd617d":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjd617dModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd617dForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd617dForm1");
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
$knjd617dCtl = new knjd617dController();
//var_dump($_REQUEST);
