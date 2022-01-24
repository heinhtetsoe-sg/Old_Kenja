<?php

require_once('for_php7.php');

require_once('knja233gModel.inc');
require_once('knja233gQuery.inc');

class knja233gController extends Controller
{
    public $ModelClassName = "knja233gModel";
    public $ProgramID      = "KNJA233G";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knja233g":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja233gModel();      //コントロールマスタの呼び出し
                    $this->callView("knja233gForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knja233gForm1");
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
$knja233gCtl = new knja233gController();
