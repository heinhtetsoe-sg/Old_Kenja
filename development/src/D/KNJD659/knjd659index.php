<?php

require_once('for_php7.php');

require_once('knjd659Model.inc');
require_once('knjd659Query.inc');

class knjd659Controller extends Controller {
    var $ModelClassName = "knjd659Model";
    var $ProgramID      = "KNJD659";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd659":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd659Model();        //コントロールマスタの呼び出し
                    $this->callView("knjd659Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd659Form1");
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
$knjd659Ctl = new knjd659Controller;
//var_dump($_REQUEST);
?>
