<?php

require_once('for_php7.php');

require_once('knja132Model.inc');
require_once('knja132Query.inc');

class knja132Controller extends Controller {
    var $ModelClassName = "knja132Model";
    var $ProgramID      = "KNJA132";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja132":								//メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja132Model();		//コントロールマスタの呼び出し
                    $this->callView("knja132Form1");
                    exit;
                case "clickchange":							//メニュー画面もしくはSUBMITした場合 //NO002
                    $sessionInstance->knja132Model();		//コントロールマスタの呼び出し
                    $this->callView("knja132Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja132Form1");
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
$knja132Ctl = new knja132Controller;
//var_dump($_REQUEST);
?>
