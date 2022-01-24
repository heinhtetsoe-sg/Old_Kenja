<?php

require_once('for_php7.php');

require_once('knjd187mModel.inc');
require_once('knjd187mQuery.inc');

class knjd187mController extends Controller
{
    public $ModelClassName = "knjd187mModel";
    public $ProgramID      = "KNJD187M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd187mModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187mForm1");
                    exit;
                case "knjd187m":                                //メニュー画面もしくはSUBMITした場合
                case "chgSeme":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjd187mModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187mForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd187mForm1");
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
$knjd187mCtl = new knjd187mController();
//var_dump($_REQUEST);
