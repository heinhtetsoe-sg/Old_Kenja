<?php

require_once('for_php7.php');

require_once('knje155Model.inc');
require_once('knje155Query.inc');

class knje155Controller extends Controller {
    var $ModelClassName = "knje155Model";
    var $ProgramID      = "KNJE155";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje155":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje155Model();        //コントロールマスタの呼び出し
                    $this->callView("knje155Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje155Form1");
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
$knje155Ctl = new knje155Controller;
//var_dump($_REQUEST);
?>
