<?php

require_once('for_php7.php');

require_once('knja131aModel.inc');
require_once('knja131aQuery.inc');

class knja131aController extends Controller {
    var $ModelClassName = "knja131aModel";
    var $ProgramID      = "KNJA131A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja131a":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja131aModel();      //コントロールマスタの呼び出し
                    $this->callView("knja131aForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合 //NO001
                    $sessionInstance->knja131aModel();      //コントロールマスタの呼び出し
                    $this->callView("knja131aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja131aForm1");
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
$knja131aCtl = new knja131aController;
//var_dump($_REQUEST);
?>
