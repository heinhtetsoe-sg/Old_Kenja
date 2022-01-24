<?php

require_once('for_php7.php');

require_once('knjd141bModel.inc');
require_once('knjd141bQuery.inc');

class knjd141bController extends Controller
{
    public $ModelClassName = "knjd141bModel";
    public $ProgramID      = "KNJD141B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd141bModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd141bForm1");
                    exit;
                case "knjd141b":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjd141bModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd141bForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd141bForm1");
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
$knjd141bCtl = new knjd141bController();
//var_dump($_REQUEST);
