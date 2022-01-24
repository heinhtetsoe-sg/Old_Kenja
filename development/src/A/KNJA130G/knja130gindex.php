<?php

require_once('for_php7.php');

require_once('knja130gModel.inc');
require_once('knja130gQuery.inc');

class knja130gController extends Controller {
    var $ModelClassName = "knja130gModel";
    var $ProgramID      = "KNJA130G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja130g":                             //メニュー画面もしくはSUBMITした場合
                case "print":
                    $sessionInstance->knja130gModel();       //コントロールマスタの呼び出し
                    $this->callView("knja130gForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合 //NO002
                    $sessionInstance->knja130gModel();       //コントロールマスタの呼び出し
                    $this->callView("knja130gForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja130gForm1");
                    }
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("print");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja130gCtl = new knja130gController;
//var_dump($_REQUEST);
?>
