<?php

require_once('for_php7.php');

require_once('knjm820Model.inc');
require_once('knjm820Query.inc');

class knjm820Controller extends Controller {
    var $ModelClassName = "knjm820Model";
    var $ProgramID      = "KNJM820";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm820":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm820Model();       //コントロールマスタの呼び出し
                    $this->callView("knjm820Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm820Form1");
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
$knjm820Ctl = new knjm820Controller;
//var_dump($_REQUEST);
?>
