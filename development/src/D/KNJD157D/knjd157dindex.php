<?php

require_once('for_php7.php');

require_once('knjd157dModel.inc');
require_once('knjd157dQuery.inc');

class knjd157dController extends Controller
{
    public $ModelClassName = "knjd157dModel";
    public $ProgramID      = "KNJD157D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd157dModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd157dForm1");
                    exit;
                case "knjd157dChangeGroupDiv":
                case "knjd157d":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjd157dModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd157dForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd157dForm1");
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
$knjd157dCtl = new knjd157dController();
