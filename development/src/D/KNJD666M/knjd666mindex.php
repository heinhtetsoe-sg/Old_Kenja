<?php

require_once('for_php7.php');

require_once('knjd666mModel.inc');
require_once('knjd666mQuery.inc');

class knjd666mController extends Controller
{
    public $ModelClassName = "knjd666mModel";
    public $ProgramID      = "KNJD666M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd666mModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd666mForm1");
                    exit;
                case "knjd666m":                                //メニュー画面もしくはSUBMITした場合
                case "chgSeme":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjd666mModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd666mForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd666mForm1");
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
$knjd666mCtl = new knjd666mController();
//var_dump($_REQUEST);
