<?php

require_once('for_php7.php');

require_once('knja130cModel.inc');
require_once('knja130cQuery.inc');

class knja130cController extends Controller
{
    public $ModelClassName = "knja130cModel";
    public $ProgramID      = "KNJA130C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja130c":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knja130cModel();       //コントロールマスタの呼び出し
                    $this->callView("knja130cForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合 //NO002
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knja130cModel();       //コントロールマスタの呼び出し
                    $this->callView("knja130cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knja130cForm1");
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
$knja130cCtl = new knja130cController();
//var_dump($_REQUEST);
