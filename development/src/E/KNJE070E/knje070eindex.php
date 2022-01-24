<?php
require_once('knje070eModel.inc');
require_once('knje070eQuery.inc');

class knje070eController extends Controller
{
    public $ModelClassName = "knje070eModel";
    public $ProgramID      = "KNJE070E";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje070e":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knje070eModel();       //コントロールマスタの呼び出し
                    $this->callView("knje070eForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje070eForm1");
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
$knje070eCtl = new knje070eController();
//var_dump($_REQUEST);
