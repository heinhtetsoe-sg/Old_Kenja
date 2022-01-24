<?php

require_once('for_php7.php');

require_once('knjd615vModel.inc');
require_once('knjd615vQuery.inc');

class knjd615vController extends Controller
{
    public $ModelClassName = "knjd615vModel";
    public $ProgramID      = "KNJD615V";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd615vModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd615vForm1");
                    exit;
                case "knjd615vChangeGroupDiv":
                case "knjd615v":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjd615vModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd615vForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd615vForm1");
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
$knjd615vCtl = new knjd615vController();
//var_dump($_REQUEST);
