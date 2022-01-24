<?php

require_once('for_php7.php');

require_once('knja131kModel.inc');
require_once('knja131kQuery.inc');

class knja131kController extends Controller {
    var $ModelClassName = "knja131kModel";
    var $ProgramID      = "KNJA131K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja131k":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja131kModel();      //コントロールマスタの呼び出し
                    $this->callView("knja131kForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合 //NO001
                    $sessionInstance->knja131kModel();      //コントロールマスタの呼び出し
                    $this->callView("knja131kForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja131kForm1");
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
$knja131kCtl = new knja131kController;
//var_dump($_REQUEST);
?>
