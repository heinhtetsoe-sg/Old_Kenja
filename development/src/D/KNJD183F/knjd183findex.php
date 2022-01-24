<?php

require_once('for_php7.php');

require_once('knjd183fModel.inc');
require_once('knjd183fQuery.inc');

class knjd183fController extends Controller
{
    public $ModelClassName = "knjd183fModel";
    public $ProgramID      = "KNJD183F";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd183fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd183fForm1");
                    exit;
                case "knjd183f":                                //メニュー画面もしくはSUBMITした場合
                case "chgSeme":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjd183fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd183fForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd183fForm1");
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
$knjd183fCtl = new knjd183fController();
//var_dump($_REQUEST);
